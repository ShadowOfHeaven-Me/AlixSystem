package alix.common.antibot.algorithms.any;

import alix.common.antibot.algorithms.any.types.ConnectionCountLimiter;
import alix.common.antibot.algorithms.any.types.TimeOutAlgo;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.netty.register.ChannelRegisteredListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.Log;

import java.net.InetAddress;

public final class ConnectRequestAlgoImpl {

    static final ChannelRegisteredListener ON_CONNECTION = ChannelRegisteredListener.of(ctx -> {
        var channel = ctx.channel();
        var addr = AlixCommonUtils.getAddress(channel);
        onConnection(channel, addr);
        new RuntimeException("ON_CONNECTION").printStackTrace();
    });

    public static void onUnregisteredConnection(Channel channel) {
        channel.pipeline().addFirst("--alix-on-connection", ON_CONNECTION);
    }

    //true on disconnect
    public static boolean onConnection(Channel channel, InetAddress ip) {
        boolean mapped = GeoIPTracker.isMapped(ip);
        TimeOutAlgo.onConnection(channel, ip, mapped);
        return ConnectionCountLimiter.onConnection(channel, ip, mapped);
    }

    public static void onLoginStartOrStatusRequest(Channel channel, InetAddress ip) {
        //new RuntimeException("onLoginStartOrStatusRequest").printStackTrace();
        TimeOutAlgo.onLoginStart(channel);
    }

    public static void close(Channel channel, ByteBuf buf) {
        LimboJoinProfiler.update(channel, ConnectionStage.CLOSE_INVOKED);
        var promise = channel.newPromise();
        channel.unsafe().write(buf, promise);
        channel.unsafe().flush();

        boolean registered = channel.isRegistered();

        if (registered)
            safeClose(channel, promise);
        else
            unsafeClose(channel, promise);
    }

    static void safeClose(Channel channel, ChannelPromise promise) {
        promise.addListener(f -> {
            if (NanoLimbo.debugAllDisconnects)
                Log.error("safeClose ConnectRequestAlgoImpl");
            channel.close();
        });
    }

    static void unsafeClose(Channel channel, ChannelPromise promise) {
        promise.addListener(f -> {
            if (NanoLimbo.debugAllDisconnects)
                Log.error("unsafeClose ConnectRequestAlgoImpl");
            closeForcibly(channel);
        });
    }

    static void closeForcibly(Channel channel) {
        if (!channel.isOpen()) return;

        channel.unsafe().closeForcibly();
        ConnectionCountLimiter.onForceClose(channel);
    }
}