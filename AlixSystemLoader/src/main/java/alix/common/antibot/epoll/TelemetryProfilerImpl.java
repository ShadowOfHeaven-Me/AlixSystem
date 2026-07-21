package alix.common.antibot.epoll;

import alix.common.AlixCommonMain;
import alix.common.antibot.epoll.data.DebugWriter;
import alix.common.antibot.epoll.syn.SynReaderWriter;
import alix.common.antibot.epoll.syn.signature.SynSignature;
import alix.common.reflection.CommonReflection;
import lombok.SneakyThrows;
import ua.nanit.limbo.NanoLimbo;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TelemetryProfilerImpl implements AbstractTelemetryProfiler {

    static final TelemetryProfilerImpl PROFILER;//new TelemetryProfilerImpl();
    static final boolean NO_DEBUG = true;

    static {
        boolean enable = NanoLimbo.INTEGRATION.isEpoll() && !NanoLimbo.INTEGRATION.isProxyProtocol();
        var opt = enable ? SynReaderWriter.createImpl() : null;
        PROFILER = opt != null ? new TelemetryProfilerImpl(opt) : null;
    }

    final SynReaderWriter SYN_OPT;
    final DebugWriter writer;

    private final Map<Integer, ConnectionRecord> activeRecords = new ConcurrentHashMap<>();

    TelemetryProfilerImpl(SynReaderWriter synOpt) {
        SYN_OPT = synOpt;
        this.writer = DebugWriter.newImpl(NO_DEBUG);
    }

    @Override
    public void enableSynSaving(int serverFd) {
        AlixCommonMain.logInfo("Enabling SynSaving for serverFd " + serverFd + "...");

        int code = SYN_OPT.enableSynSaving0(serverFd);
        if (code != 0)
            AlixCommonMain.logInfo("Failed to set TCP_SAVE_SYN. Error code: " + code);
        else
            AlixCommonMain.logInfo("Successfully set TCP_SAVE_SYN.");
    }

    @Override
    public ConnectionRecord record(int clientFd) {
        return this.activeRecords.get(clientFd);
    }

    private static final Method getTcpInfo;

    static {
        getTcpInfo = CommonReflection.getDeclaredMethodAccessible(CommonReflection.forName("io.netty.channel.epoll.LinuxSocket"), "getTcpInfo", int.class, long[].class);
        getTcpInfo.setAccessible(true);
    }

    static void tcpSample(ConnectionRecord record, SampleTrigger trigger) {
        if (NO_DEBUG) return;

        int clientFd = record.clientFd;
        long[] info = record.info;// (long[]) CommonReflection.get(tcpInfo, epollTcpInfo);

        if (record.closed) return;
        try {
            CommonReflection.invoke(getTcpInfo, null, clientFd, info);
        } catch (Throwable t) {
            return;//was closed, ignore?
        }

        if (!record.closed)
            record.tcpSamples.add(new TcpSample(info, trigger));
    }

    @SneakyThrows
    private String addr(byte[] addr) {
        if (NO_DEBUG)
            return null;

        switch (addr[0]) {
            case 8:
                return (addr[1] & 0xff) + "." + (addr[2] & 0xff) + "." + (addr[3] & 0xff) + "." + (addr[4] & 0xff);
            case 24:
                byte[] byteAddr = new byte[16];
                System.arraycopy(addr, 1, byteAddr, 0, 16);
                return InetAddress.getByAddress(byteAddr).getHostAddress();
            default:
                throw new Error();
        }
    }

    @Override
    public void onConnection(int clientFd, byte[] addr) {
        ConnectionRecord record = new ConnectionRecord(clientFd, this.parseSynSignature(clientFd), this.addr(addr));
        activeRecords.put(clientFd, record);
        tcpSample(record, SampleTrigger.CONNECT);

        //AlixScheduler.runLaterAsync(() -> this.remove(clientFd), 1, TimeUnit.SECONDS);
    }

    @Override
    public void onLoginStart(int clientFd) {
        ConnectionRecord record = activeRecords.get(clientFd);
        if (record == null)
            return;

        tcpSample(record, SampleTrigger.L7_LOGIN_START);
    }

    @Override
    public void onStatusRequest(int clientFd) {
        ConnectionRecord record = activeRecords.get(clientFd);
        if (record == null)
            return;

        tcpSample(record, SampleTrigger.L7_STATUS_REQUEST);
    }

    @Override
    public void onHandshake(int clientFd, int nextState) {
        ConnectionRecord record = activeRecords.get(clientFd);
        if (record == null)
            return;

        record.handshakeTimestamp = System.currentTimeMillis();
        record.nextState = nextState;

        tcpSample(record, SampleTrigger.L7_HANDSHAKE);
        //writeToDisk(record);
    }

    @Override
    public void removeClosed(int clientFd) {
        this.remove(clientFd);
    }

    void remove(int clientFd) {
        ConnectionRecord record = activeRecords.remove(clientFd);
        if (record != null) {
            //tcpSample(record, trigger);
            record.closed = true;
            this.writer.writeToDisk(record);
        }
    }

    private SynSignature parseSynSignature(int fd) {
        return SYN_OPT.parseSynSignature(fd);
    }

    public static final class ConnectionRecord {
        public final long acceptTimestamp = System.currentTimeMillis();
        final long[] info = NO_DEBUG ? null : new long[32];
        private final int clientFd;
        public final String addr;
        public long handshakeTimestamp = -1;
        public int nextState = -1;
        public ConnectionStats stats;
        //public TrafficHeuristics.Result evaluation;

        volatile boolean closed;

        public final SynSignature synSignature;
        public final List<TcpSample> tcpSamples = NO_DEBUG ? List.of() : new ArrayList<>(3);

        public ConnectionRecord(int clientFd, SynSignature synSignature, String addr) {
            this.clientFd = clientFd;
            this.synSignature = synSignature;
            this.addr = addr;
        }

        public long getL7DeltaT() {
            return handshakeTimestamp != -1 ? (handshakeTimestamp - acceptTimestamp) : -1;
        }

        public long getAgeMs() {
            return System.currentTimeMillis() - this.acceptTimestamp;
        }
    }

    public enum SampleTrigger {
        CONNECT,
        PERIODIC,
        L7_HANDSHAKE,
        L7_LOGIN_START,
        L7_STATUS_REQUEST,
        CHANNEL_INACTIVE,
        TIMED
    }

    public static final class TcpSample {
        private final int retransmits;
        private final int probes;
        private final int backoff;
        private final long unacked;
        private final long sacked;
        private final long lost;
        private final long retrans;
        private final long rtt;
        private final long rttvar;
        private final long sndCwnd;
        private final long reordering;
        private final long rcvRtt;
        private final long totalRetrans;
        private final SampleTrigger trigger;
        private final long timestamp;

        public TcpSample(
                int retransmits,
                int probes,
                int backoff,
                long unacked,
                long sacked,
                long lost,
                long retrans,
                long rtt,
                long rttvar,
                long sndCwnd,
                long reordering,
                long rcvRtt,
                long totalRetrans,
                SampleTrigger trigger,
                long timestamp
        ) {
            this.retransmits = retransmits;
            this.probes = probes;
            this.backoff = backoff;
            this.unacked = unacked;
            this.sacked = sacked;
            this.lost = lost;
            this.retrans = retrans;
            this.rtt = rtt;
            this.rttvar = rttvar;
            this.sndCwnd = sndCwnd;
            this.reordering = reordering;
            this.rcvRtt = rcvRtt;
            this.totalRetrans = totalRetrans;
            this.trigger = trigger;
            this.timestamp = timestamp;
        }

        public TcpSample(long[] info, SampleTrigger trigger) {
            this(
                    (int) info[2], (int) info[3],
                    (int) info[4], info[12], info[13], info[14], info[15],
                    info[23], info[24],
                    info[26], info[28], info[29], info[31],
                    trigger, System.currentTimeMillis()
            );
        }

        public int retransmits() {
            return retransmits;
        }

        public int probes() {
            return probes;
        }

        public int backoff() {
            return backoff;
        }

        public long unacked() {
            return unacked;
        }

        public long sacked() {
            return sacked;
        }

        public long lost() {
            return lost;
        }

        public long retrans() {
            return retrans;
        }

        public long rtt() {
            return rtt;
        }

        public long rttvar() {
            return rttvar;
        }

        public long sndCwnd() {
            return sndCwnd;
        }

        public long reordering() {
            return reordering;
        }

        public long totalRetrans() {
            return totalRetrans;
        }

        public long timestamp() {
            return timestamp;
        }

    }
}