package alix.common.antibot.algorithms.any.types;

import alix.common.antibot.algorithms.any.ConnectRequestAlgoImpl;
import alix.common.antibot.score.IpScoreAnalysis;
import alix.common.antibot.score.IpScoreHint;
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

    private static final Map<ChannelId, State> STATES = new ConcurrentHashMap<>(1 << 10); // 1024

    //todo: remove stale
    private static final Map<InetAddress, TimeOutInfo> TIMEOUT_COUNT = new ConcurrentHashMap<>(1 << 8);  // 256
    private static final ByteBuf TIMED_OUT_BUF;

    private static final long TIME_WINDOW_MS = 60_000L;
    private static final int MAX_TIMEOUTS = 10;

    static {
        var reason = Component.text("Timed Out").color(NamedTextColor.YELLOW);
        TIMED_OUT_BUF = PacketUtils.constLoginDisconnect(reason);
    }

    static State state(Channel channel) {
        return STATES.computeIfAbsent(channel.id(), id -> new State());
    }

    public static void onConnection(Channel channel, InetAddress addr, boolean mapped) {
        channel.eventLoop().execute(() -> onConnection0(channel, addr, mapped));
    }

    static void onConnection0(Channel channel, InetAddress addr, boolean mapped) {
        State state = state(channel);

        // login start already won the race, do nothing
        if (state.stage == LOGIN_START)
            return;

        // more leeway for mapped addresses
        int timeout = mapped ? 12 : 7;

        var future = channel.eventLoop().schedule(() -> {
            if (!state.compareAndSet(STARTED, TIMED_OUT))
                return;//login start has already outdone us

            LimboJoinProfiler.update(channel, ConnectionStage.TIMED_OUT);

            ConnectRequestAlgoImpl.close(channel, TIMED_OUT_BUF);

            var info = TIMEOUT_COUNT.computeIfAbsent(addr, w -> new TimeOutInfo());
            if (info.addTimeout(addr))
                TIMEOUT_COUNT.remove(addr);

        }, timeout, TimeUnit.SECONDS);

        state.timeout = future;

        if (!state.compareAndSet(INIT, STARTED)) {
            //login start arrived
            cancel(state);
            return;
        }

        channel.closeFuture().addListener(f -> remove0(channel));
    }

    private static void remove0(Channel channel) {
        State state = STATES.remove(channel.id());
        if (state == null)
            return;

        cancel(state);
    }

    public static void onLoginStart(Channel channel) {
        State state = state(channel);
        state.stage = LOGIN_START;

        cancel(state);
    }

    static void cancel(State state) {
        ScheduledFuture<?> future = state.timeout;
        if (future != null) {
            state.timeout = null;
            future.cancel(false);
        }
    }

    private static final int INIT = 0;
    private static final int STARTED = 1;
    private static final int LOGIN_START = 2;
    private static final int TIMED_OUT = 3;

    private static final class State {
        int stage = INIT;
        ScheduledFuture<?> timeout;

        boolean compareAndSet(int expected, int newValue) {
            if (this.stage == expected) {
                this.stage = newValue;
                return true;
            }
            return false;
        }
    }

    private static final class TimeOutInfo {

        private final ConcurrentLinkedDeque<Long> lastDisconnects = new ConcurrentLinkedDeque<>();

        private void expungeStale() {
            long staleThreshold = System.currentTimeMillis() - TIME_WINDOW_MS;
            Long oldest;

            while ((oldest = this.lastDisconnects.peekFirst()) != null && oldest < staleThreshold)
                this.lastDisconnects.pollFirst();
        }

        boolean addTimeout(InetAddress addr) {
            this.expungeStale();

            long now = System.currentTimeMillis();
            this.lastDisconnects.add(now);
            IpScoreAnalysis.hint(addr, IpScoreHint.TIMED_OUT);

            /*if (this.lastDisconnects.size() >= MAX_TIMEOUTS) {
                //FireWallManager.add(addr, AlgorithmId.H2, true);

                this.lastDisconnects.clear();
                return true;
            }*/
            return false;
        }
    }
}