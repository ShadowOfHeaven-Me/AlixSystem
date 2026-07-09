package alix.velocity.utils;


import alix.common.antibot.epoll.Telemetry;
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
    private static final Multimap<InetSocketAddress, Endpoint> endpoints = getEndpoints();

    public static void initEndpoints() {
        endpoints.values().forEach(e -> {
            var ch = e.getChannel();
            if (!INIT.add(ch))
                return;

            //var initHolder = MANAGER.getServerChannelInitializer();
            ch.pipeline().addFirst("--alix_init", new ServerChannelInitializer());

            if (!Telemetry.ENABLED || !(ch instanceof EpollServerSocketChannel serverChannel))
                return;

            TelemetryProfiler.enableSynSaving(serverChannel.fd().intValue());
        });
    }

    public static boolean isEpoll() {
        return endpoints.values().stream().anyMatch(endpoint -> endpoint.getChannel() instanceof EpollServerSocketChannel);
    }

    private static final Set<Channel> INIT = ConcurrentHashMap.newKeySet(); //Collections.newSetFromMap(Collections.synchronizedMap(new IdentityHashMap<>()));

    /*@SneakyThrows
    public static void initializeInterceptor() {
        var initHolder = MANAGER.getServerChannelInitializer();

        initHolder.set(new ServerChannelInitializer(initHolder.get()));
    }*/

    @SneakyThrows
    private static Multimap<InetSocketAddress, Endpoint> getEndpoints() {
        ConnectionManager manager = MANAGER;
        var f = CommonReflection.getDeclaredFieldAccessible(ConnectionManager.class, "endpoints");
        return (Multimap<InetSocketAddress, Endpoint>) f.get(manager);
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