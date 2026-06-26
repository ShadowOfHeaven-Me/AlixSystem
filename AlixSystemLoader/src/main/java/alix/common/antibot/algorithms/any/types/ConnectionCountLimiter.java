package alix.common.antibot.algorithms.any.types;

import alix.common.antibot.algorithms.any.ConnectRequestAlgoImpl;
import alix.common.antibot.algorithms.any.PanicModeManager;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.epoll.Telemetry;
import alix.common.antibot.epoll.TelemetryProfiler;
import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigParams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ua.nanit.limbo.protocol.packets.PacketUtils;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class ConnectionCountLimiter {

    //private static final Map<InetAddress, Integer> MAPPED_IPS = new ConcurrentHashMap<>(1 << 10);//1024
    private static final Map<InetAddress, LongAdder> CONNECTIONS = new ConcurrentHashMap<>(1 << 10);

    private static final int HARD_LIMIT = 2000;
    private static final LongAdder TOTAL = new LongAdder();

    private static final ByteBuf ACCOUNTS_EXCEEDED, TOTAL_EXCEEDED;
    //private static final int maxConnections = ;

    static {
        ACCOUNTS_EXCEEDED = PacketUtils.constLoginDisconnect(Component.text("Current connection count per IP exceeded").color(NamedTextColor.YELLOW));
        TOTAL_EXCEEDED = PacketUtils.constLoginDisconnect(Component.text("Total connections exceeded").color(NamedTextColor.YELLOW));
    }

    public static void onForceClose(Channel channel) {
        var c = CONNECTIONS.get(AlixCommonUtils.getAddress(channel));
        if (c != null)
            c.decrement();

        TOTAL.decrement();
    }

    private static int maxConnections() {
        int max = AntiBotStatistics.INSTANCE.isHighTraffic() ? 3 :
                (ConfigParams.maximumTotalAccounts > 0 ? ConfigParams.maximumTotalAccounts : 3);
        return max;
    }

    public static boolean onConnection(Channel channel, InetAddress ip, boolean mapped) {
        if (TOTAL.sum() >= HARD_LIMIT && !mapped) {
            LimboJoinProfiler.update(channel, ConnectionStage.TOTAL_CONNECTIONS_CNT_LIMIT_REACHED);
            ConnectRequestAlgoImpl.close(channel, TOTAL_EXCEEDED);
            PanicModeManager.activate(HARD_LIMIT + " connections exceeded!");
            return true;
        }

        var c = CONNECTIONS.computeIfAbsent(ip, w -> new LongAdder());

        long sum;
        int maxConnections;
        //if currently is equal, then this connection would force it to be above max
        if (!mapped && (sum = c.sum()) >= (maxConnections = maxConnections())) {
            if (LimboJoinProfiler.PROFILE_JOINS)
                LimboJoinProfiler.update(channel, ConnectionStage.LOCAL_CONNECTIONS_CNT_LIMIT_REACHED, "sum=" + sum + " maxConnections=" + maxConnections);
            ConnectRequestAlgoImpl.close(channel, ACCOUNTS_EXCEEDED);
            return true;
        }

        c.increment();
        TOTAL.increment();

        channel.closeFuture().addListener(f -> {
            if (Telemetry.ENABLED)
                TelemetryProfiler.PROFILER.removeClosed(channel);
            c.decrement();
            TOTAL.decrement();
            LimboJoinProfiler.update(channel, ConnectionStage.CLOSE_FUTURE_FINISHED);
        });
        return false;
    }
}