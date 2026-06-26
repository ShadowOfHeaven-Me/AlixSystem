package alix.common.antibot.algorithms.any;

import alix.common.antibot.algorithms.any.types.ConnectionCountLimiter;
import alix.common.antibot.algorithms.any.types.TimeOutAlgo;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;

import java.net.InetAddress;

public final class ConnectRequestAlgoImpl {

    //true on disconnect
    public static boolean onConnection(Channel channel, InetAddress ip) {
        boolean mapped = GeoIPTracker.isMapped(ip);
        TimeOutAlgo.onConnection(channel, ip, mapped);
        return ConnectionCountLimiter.onConnection(channel, ip, mapped);
    }

    public static void onLoginStartOrStatusRequest(Channel channel, InetAddress ip) {
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
            channel.close();
        });
    }

    static void unsafeClose(Channel channel, ChannelPromise promise) {
        promise.addListener(f -> {
            closeForcibly(channel);
        });
    }

    static void closeForcibly(Channel channel) {
        if (!channel.isOpen()) return;

        channel.unsafe().closeForcibly();
        ConnectionCountLimiter.onForceClose(channel);
    }
}