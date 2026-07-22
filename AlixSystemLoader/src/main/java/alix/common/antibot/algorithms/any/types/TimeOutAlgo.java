package alix.common.antibot.algorithms.any.types;

import alix.common.antibot.algorithms.any.ConnectRequestAlgoImpl;
import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.utils.AlixClock;
import alix.common.utils.netty.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.packets.login.PacketLoginPluginRequest;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class TimeOutAlgo {

    private static final ByteBuf PLUGIN_REQUEST;
    private static final Map<ChannelId, State> STATES = new ConcurrentHashMap<>(1 << 10); // 1024

    //todo: remove stale
    private static final Map<InetAddress, TimeOutInfo> TIMEOUT_COUNT = new ConcurrentHashMap<>(1 << 8);  // 256
    private static final ByteBuf TIMED_OUT_BUF;

    private static final long TIME_WINDOW_MS = 60_000L;
    private static final int MAX_TIMEOUTS = 10;

    private static final ByteBuf ONE_ZERO_BYTE;

    static {
        var reason = Component.text("Timed Out").color(NamedTextColor.YELLOW);
        TIMED_OUT_BUF = PacketUtils.constLoginDisconnect(reason);

        var pluginRequest = PacketLoginPluginRequest.of(0, "0:0", new byte[0]);
        PLUGIN_REQUEST = PacketUtils.constClientbound(pluginRequest);

        var buffer = BufUtils.UNPOOLED.buffer(1, 1);
        buffer.writeByte(0);
        ONE_ZERO_BYTE = BufUtils.constBuffer(buffer);
    }

    static State state(Channel channel) {
        return STATES.computeIfAbsent(channel.id(), id -> new State());
    }

    public static void onConnection(Channel channel, InetAddress addr, boolean mapped) {
        channel.eventLoop().execute(() -> onConnection0(channel, addr, mapped));
    }

    static void onConnection0(Channel channel, InetAddress addr, boolean mapped) {
        State state = state(channel);
        state.setConn();

        // login start already won the race, do nothing
        if (state.stage == LOGIN_START)
            return;

        // more leeway for mapped addresses
        int timeout = mapped ? 12 : 7;

        //int pluginReq = timeout >> 1;

        //long now = AlixClock.currentTimeMillis();

        /*channel.unsafe().write(ONE_ZERO_BYTE, channel.voidPromise());
        channel.unsafe().flush();*/
        /*channel.eventLoop().schedule(() -> {

        }, pluginReq, TimeUnit.SECONDS);*/

        var future = channel.eventLoop().schedule(() -> {
            if (!state.compareAndSet(STARTED, TIMED_OUT))
                return;//login start has already outdone us

            // Inspect socket state at time of expiration
            /*if (channel instanceof EpollSocketChannel epollChannel) {
                var tcpInfo = epollChannel.tcpInfo();
                // If lastAckRecv is very high, client kernel disappeared (dead connection)
                // If low, connection is alive at L4 but sitting idle at L7
                long lastAckMs = tcpInfo.lastAckRecv();
                AlixCommonMain.logInfo("Connection timed out. Last L4 ACK was " + lastAckMs + "ms ago, rtt=" + tcpInfo.rtt() / 1000 +
                                       "ms rttVar=" + tcpInfo.rttvar() + " rcvRtt=" + tcpInfo.rcvRtt() + " backoff=" + tcpInfo.backoff() +
                                       "totalRetrans=" + tcpInfo.totalRetrans());
            }*/

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
        state.setRcvL7();
        state.stage = LOGIN_START;

        //var tcpInfo = ((EpollSocketChannel) channel).tcpInfo();
        //AlixCommonMain.logInfo("L7: LAST ACK=" + tcpInfo.lastAckRecv() + " rtt=" + tcpInfo.rtt() / 1000);

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

        long conn, l7;

        void setConn() {
            this.conn = AlixClock.currentTimeMillis();
            this.printIfBoth();
        }

        void setRcvL7() {
            this.l7 = AlixClock.currentTimeMillis();
            this.printIfBoth();
        }

        void printIfBoth() {
            if (this.conn == 0 || this.l7 == 0) return;

            //AlixCommonMain.logWarning("L7 TOOK=" + (this.l7 - this.conn) + "ms");
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
            //AlixCommonMain.logWarning("Timed Out=" + addr);
            //IpScoreAnalysis.hint(addr, IpScoreHint.TIMED_OUT);

            /*if (this.lastDisconnects.size() >= MAX_TIMEOUTS) {
                //FireWallManager.add(addr, AlgorithmId.H2, true);

                this.lastDisconnects.clear();
                return true;
            }*/
            return false;
        }
    }
}