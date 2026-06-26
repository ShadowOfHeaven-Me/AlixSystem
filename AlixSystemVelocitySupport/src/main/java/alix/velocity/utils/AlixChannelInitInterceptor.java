package alix.velocity.utils;


import alix.common.antibot.epoll.TelemetryProfiler;
import alix.common.reflection.CommonReflection;
import alix.common.utils.other.throwable.AlixException;
import alix.loaders.velocity.VelocityAlixMain;
import alix.velocity.systems.channel.ServerChannelInitializer;
import com.google.common.collect.Multimap;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;
import com.velocitypowered.proxy.network.Endpoint;
import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AlixChannelInitInterceptor {

    private static final ConnectionManager MANAGER = getManager();

    @SneakyThrows
    public static void initSynFingerprint() {
        ConnectionManager manager = MANAGER;
        var f = CommonReflection.getDeclaredFieldAccessible(ConnectionManager.class, "endpoints");
        Multimap<InetSocketAddress, Endpoint> endpoints = (Multimap<InetSocketAddress, Endpoint>) f.get(manager);

        endpoints.values().forEach(e -> {
            if (!INIT.add(e.getChannel()))
                return;
            var serverChannel = (EpollServerSocketChannel) e.getChannel();
            TelemetryProfiler.enableSynSaving(serverChannel.fd().intValue());
        });
    }

    private static final Set<Channel> INIT = ConcurrentHashMap.newKeySet(); //Collections.newSetFromMap(Collections.synchronizedMap(new IdentityHashMap<>()));

    @SneakyThrows
    public static void initializeInterceptor(VelocityServer server) {
        ConnectionManager m = MANAGER;
        var initHolder = m.getServerChannelInitializer();

        initHolder.set(new ServerChannelInitializer(server, initHolder.get()));
    }

    @SneakyThrows
    private static ConnectionManager getManager() {
        VelocityServer server = (VelocityServer) VelocityAlixMain.instance.getServer();
        for (Field f : server.getClass().getDeclaredFields()) {
            if (f.getType() == ConnectionManager.class) {
                f.setAccessible(true);
                return (ConnectionManager) f.get(server);
            }
        }
        throw new AlixException("ConnectionManager is missing!");
    }
}