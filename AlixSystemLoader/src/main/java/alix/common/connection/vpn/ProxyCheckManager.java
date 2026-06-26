package alix.common.connection.vpn;

import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.connection.vpn.impl.IP2ProxyImpl;
import alix.common.connection.vpn.impl.IPAPIImpl;
import alix.common.connection.vpn.impl.KauriImpl;
import alix.common.connection.vpn.impl.ProxyCheckIOImpl;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.list.LoopList;
import alix.common.utils.file.managers.IpsCacheFileManager;
import io.netty.channel.Channel;

import java.net.InetAddress;
import java.util.function.Consumer;

public final class ProxyCheckManager {

    public static final ProxyCheckManager INSTANCE = new ProxyCheckManager();
    private final LoopList<ProxyCheck> slowRegen;
    private final LoopList<ProxyCheck> fastRegen;
    //private boolean warned;

    private ProxyCheckManager() {//request max:     1000/day             500/day       20,000/month
        this.slowRegen = LoopList.newConcurrent(new ProxyCheckIOImpl(), new IP2ProxyImpl(), new KauriImpl());
        this.fastRegen = LoopList.newConcurrent(new IPAPIImpl());//up to 45 req/min
    }

    public void isProxy(Channel channel, InetAddress ip, String strAddress, Consumer<Boolean> isProxyCallback) {
        //Use the cached value to reduce request usage
        Boolean cache = IpsCacheFileManager.isProxy(ip);
        if (cache != null) {
            isProxyCallback.accept(cache);
            return;
        }

        AlixScheduler.asyncBlocking(() -> {
            LimboJoinProfiler.update(channel, ConnectionStage.VPN_BLOCKING_REQUESTED);

            Boolean isProxy = this.isProxy0(strAddress, this.fastRegen);
            if (isProxy == null) isProxy = this.isProxy0(strAddress, this.slowRegen);

            //if isProxy is null (couldn't be determined) we return that the ip is not a proxy
            boolean proxy = isProxy == Boolean.TRUE;

            if (isProxy != null)
                IpsCacheFileManager.add(ip, proxy);

            isProxyCallback.accept(proxy);
        });
    }

    //returns null if couldn't be determined
    private Boolean isProxy0(String ip, LoopList<ProxyCheck> list) {
        CheckResult result;
        int looped = 0;
        while ((result = list.current().isProxy(ip)) == CheckResult.UNAVAILABLE) {
            list.nextIndex();
            if (++looped == list.size())//we looped through every provider in this very loop, all of which returned unavailable. It's time to panic
                return null;//say we dunno
        }
        return result == CheckResult.PROXY ? Boolean.TRUE : result == CheckResult.NON_PROXY ? Boolean.FALSE : null;
    }

    /*if (!warned) {
                    Main.logWarning("");
                    Main.logWarning("[WARNING]");
                    Main.logWarning("");
                    Main.logWarning("All VPN providers are unavailable! No VPN checks can be made any more!");
                    Main.logWarning("");
                    Main.logWarning("[WARNING]");
                    Main.logWarning("");
                }
                this.warned = true;*/
}