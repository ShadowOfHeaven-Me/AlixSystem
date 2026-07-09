package alix.common.antibot.algorithms.any.types;

import alix.common.antibot.algorithms.any.ConnectRequestAlgoImpl;
import alix.common.antibot.firewall.AlgorithmId;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ua.nanit.limbo.protocol.packets.PacketUtils;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class TimeOutAlgo {

    private static final Map<ChannelId, ScheduledFuture<?>> TIMEOUTS = new ConcurrentHashMap<>(1 << 10); // 1024

    //todo: remove stale
    private static final Map<InetAddress, TimeOutInfo> TIMEOUT_COUNT = new ConcurrentHashMap<>(1 << 8);  // 256
    private static final ByteBuf TIMED_OUT;

    // --- Configuration Constants ---
    private static final long TIME_WINDOW_MS = 60_000L; // How long to remember a timeout (e.g., 1 minute)
    private static final int MAX_TIMEOUTS = 10;          // How many timeouts before taking action

    static {
        var reason = Component.text("Timed Out").color(NamedTextColor.YELLOW);
        TIMED_OUT = PacketUtils.constLoginDisconnect(reason);
    }

    public static void onConnection(Channel channel, InetAddress addr, boolean mapped) {
        /*AlixCommonMain.logInfo("onConnection " + channel.id());
        new Exception().printStackTrace();*/

        int timeout = mapped ? 12 : 7; // more leeway for mapped addresses
        TIMEOUTS.put(channel.id(), channel.eventLoop().schedule(() -> {
            LimboJoinProfiler.update(channel, ConnectionStage.TIMED_OUT);

            ConnectRequestAlgoImpl.close(channel, TIMED_OUT);

            var info = TIMEOUT_COUNT.computeIfAbsent(addr, w -> new TimeOutInfo());
            if (info.addTimeout(addr))
                TIMEOUT_COUNT.remove(addr);

        }, timeout, TimeUnit.SECONDS));

        channel.closeFuture().addListener(f -> remove(channel));
    }

    private static final class TimeOutInfo {

        private final ConcurrentLinkedDeque<Long> lastDisconnects = new ConcurrentLinkedDeque<>();

        private void expungeStale() {
            long staleThreshold = System.currentTimeMillis() - TIME_WINDOW_MS;
            Long oldest;

            // We use peekFirst/pollFirst since timestamps are added sequentially.
            // This is thread-safe and avoids iterating over the entire collection.
            while ((oldest = this.lastDisconnects.peekFirst()) != null && oldest < staleThreshold) {
                this.lastDisconnects.pollFirst();
            }
        }

        boolean addTimeout(InetAddress addr) {
            this.expungeStale();

            long now = System.currentTimeMillis();
            this.lastDisconnects.add(now);

            if (this.lastDisconnects.size() >= MAX_TIMEOUTS) {
                FireWallManager.add(addr, AlgorithmId.H1, true);

                this.lastDisconnects.clear();
                return true;
            }
            return false;
        }
    }

    private static void remove(Channel channel) {
        /*AlixCommonMain.logInfo("REMOVE " + channel.id());
        new Exception().printStackTrace();*/
        var future = TIMEOUTS.remove(channel.id());
        if (future != null) {
            future.cancel(false);
        }
    }

    public static void onLoginStart(Channel channel) {
        remove(channel);
    }
}