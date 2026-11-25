package alix.common.connection.vpn;

import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.connection.vpn.impl.*;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.collections.list.LoopList;
import alix.common.utils.file.managers.IpsCacheFileManager;
import io.netty.channel.Channel;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ProxyCheckManager {

    public static final ProxyCheckManager INSTANCE = new ProxyCheckManager();

    // ip -> ip info
    private final Map<String, CompletableFuture<IPInfo>> inFlightRequests = new ConcurrentHashMap<>();

    private final LoopList<ProxyCheck> primaryProviders;
    private final LoopList<ProxyCheck> fallbackProviders;

    private ProxyCheckManager() {
        this.primaryProviders = LoopList.newConcurrent(
                new IPAPIImpl(),//45 req/min
                new IPApiIsImpl(),//1000/day
                new ProxyCheckIOImpl(),//100/day for unregistered, 1000/day with an api key
                new VPNDetectionImpl()//no key, no daily cap
        );

        this.fallbackProviders = LoopList.newConcurrent(
                new IP2ProxyImpl(),
                new KauriImpl()
        );
    }


    public void checkIp(Channel channel, InetAddress ip, String strAddress, Consumer<IPInfo> callback) {
        IPInfo cached = IpsCacheFileManager.getInfo(ip);
        if (cached != null) {
            callback.accept(cached);
            return;
        }

        // Deduplicate ongoing requests for the same IP address
        inFlightRequests.computeIfAbsent(strAddress, address -> {
            CompletableFuture<IPInfo> future = new CompletableFuture<>();

            AlixScheduler.asyncBlocking(() -> {
                try {
                    LimboJoinProfiler.update(channel, ConnectionStage.VPN_BLOCKING_REQUESTED);

                    // Primary Providers
                    CheckResultHolder result = this.check(address, this.primaryProviders);

                    // Fallback Providers
                    if (result.getStatus() == CheckResult.UNAVAILABLE) {
                        result = this.check(address, this.fallbackProviders);
                    }

                    IPInfo info;
                    if (result.getStatus() != CheckResult.UNAVAILABLE && result.getInfo() != null) {
                        info = result.getInfo();
                        IpsCacheFileManager.add(ip, info);
                        future.complete(info);
                    } else {
                        future.complete(null);
                    }

                } catch (Throwable t) {
                    //future.completeExceptionally(t);
                    AlixCommonUtils.logException(t);
                    future.complete(null);
                } finally {
                    inFlightRequests.remove(address);
                }
            });

            return future;
        }).thenAccept(callback);
    }

    public void isProxy(Channel channel, InetAddress ip, String strAddress, Consumer<Boolean> isProxyCallback) {
        checkIp(channel, ip, strAddress, info -> isProxyCallback.accept(info != null && info.isProxy()));
    }

    private CheckResultHolder check(String ip, LoopList<ProxyCheck> providers) {
        int count = 0;
        int size = providers.size();

        while (count < size) {
            ProxyCheck currentProvider = providers.current();
            providers.nextIndex();
            count++;

            CheckResultHolder result = currentProvider.isProxy(ip);
            if (result.getStatus() != CheckResult.UNAVAILABLE) {
                return result;
            }
        }

        return CheckResultHolder.UNAVAILABLE;
    }
}