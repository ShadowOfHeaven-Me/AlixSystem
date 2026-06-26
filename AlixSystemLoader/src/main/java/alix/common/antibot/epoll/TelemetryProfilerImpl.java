package alix.common.antibot.epoll;

import alix.common.AlixCommonMain;
import alix.common.reflection.CommonReflection;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.epoll.EpollTcpInfo;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

final class TelemetryProfilerImpl implements AbstractTelemetryProfiler {

    // --- Linux Constants ---
    private static final int IPPROTO_TCP = 6;
    private static final int TCP_SAVE_SYN = 27;  // Option to SET on the server
    private static final int TCP_SAVED_SYN = 28; // Option to READ on the client

    private static final MethodHandle GETSOCKOPT;
    private static final MethodHandle SETSOCKOPT;

    static final TelemetryProfilerImpl PROFILER = new TelemetryProfilerImpl();

    static {
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();

        GETSOCKOPT = linker.downcallHandle(
                stdlib.find("getsockopt").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        SETSOCKOPT = linker.downcallHandle(
                stdlib.find("setsockopt").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );
    }

    private final Map<Integer, ConnectionRecord> activeRecords = new ConcurrentHashMap<>();

    @Override
    public void enableSynSaving0(int serverFd) {
        AlixCommonMain.logInfo("Enabling SynSaving for serverFd " + serverFd + "...");
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment optval = arena.allocate(ValueLayout.JAVA_INT, 1);
            int result = (int) SETSOCKOPT.invokeExact(serverFd, IPPROTO_TCP, TCP_SAVE_SYN, optval, 4);
            if (result != 0) {
                AlixCommonMain.logInfo("[Telemetry] Failed to set TCP_SAVE_SYN. Error code: " + result);
            } else
                AlixCommonMain.logInfo("[Telemetry] Successfully set TCP_SAVE_SYN.");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static final Method getTcpInfo;
    private static final Field tcpInfo;

    static {
        getTcpInfo = CommonReflection.getDeclaredMethod(CommonReflection.forName("io.netty.channel.epoll.LinuxSocket"), "getTcpInfo", int.class, long[].class);
        getTcpInfo.setAccessible(true);

        tcpInfo = CommonReflection.getDeclaredFieldAccessible(EpollTcpInfo.class, "info");

        AlixScheduler.repeatAsync(() -> {
            var list = PROFILER.activeRecords.values().stream().toList();

            list.forEach(record -> {
                tcpSample(record, SampleTrigger.PERIODIC);
            });
        }, 50, TimeUnit.MILLISECONDS);
    }

    static void tcpSample(ConnectionRecord record, SampleTrigger trigger) {
        if (record.closed) return;

        int clientFd = record.clientFd;
        long[] info = new long[32];// (long[]) CommonReflection.get(tcpInfo, epollTcpInfo);

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

    public void onConnection(int clientFd, byte[] addr) {
        ConnectionRecord record = new ConnectionRecord(clientFd, this.addr(addr));
        record.synSignature = parseSynSignature(clientFd);
        activeRecords.put(clientFd, record);
        tcpSample(record, SampleTrigger.CONNECT);

        AlixScheduler.runLaterAsync(() -> {
            this.remove(clientFd, SampleTrigger.TIMED_OUT);
        }, 7, TimeUnit.SECONDS);
    }

    int clientFd(Channel channel) {
        return ((EpollSocketChannel) channel).fd().intValue();
    }

    public void onLoginStart(int clientFd) {
        ConnectionRecord record = activeRecords.get(clientFd);
        if (record == null)
            return;

        tcpSample(record, SampleTrigger.L7_LOGIN_START);
    }

    public void onStatusRequest(int clientFd) {
        ConnectionRecord record = activeRecords.get(clientFd);
        if (record == null)
            return;

        tcpSample(record, SampleTrigger.L7_STATUS_REQUEST);
    }

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
        this.remove(clientFd, SampleTrigger.CHANNEL_INACTIVE);
    }

    ConnectionRecord remove(int clientFd, SampleTrigger trigger) {
        ConnectionRecord record = activeRecords.remove(clientFd);
        if (record != null) {
            tcpSample(record, trigger);
            record.closed = true;
            writeToDisk(record);
        }
        return record;
    }

    @SneakyThrows
    private static BufferedWriter output() {
        var folder = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolder(), "out");
        var f = new File(folder, AlixCommonUtils.getFormattedDate(new Date()) + ".txt");
        folder.mkdir();
        f.createNewFile();

        return Files.newBufferedWriter(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    private final BufferedWriter out = output();
    private final ReentrantLock lock = new ReentrantLock();

    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.out.close();
                AlixCommonMain.logInfo("Closed out");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ConnectionRecord.class, new ConnectionRecordSerializer())
            .create();

    private void writeToDisk(ConnectionRecord record) {
        Thread.startVirtualThread(() -> {
            var stats = new ConnectionStats(record.tcpSamples);
            record.stats = stats;
            record.evaluation = TrafficHeuristics.evaluate(record, stats);

            this.lock.lock();
            try {
                this.out.write(GSON.toJson(record) + "\n");
                this.out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                this.lock.unlock();
            }
        });
    }

    private SynSignature parseSynSignature(int fd) {
        SynSignature sig = new SynSignature();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment optval = arena.allocate(256);
            MemorySegment optlen = arena.allocate(ValueLayout.JAVA_INT, (int) optval.byteSize());

            int result = (int) GETSOCKOPT.invokeExact(fd, IPPROTO_TCP, TCP_SAVED_SYN, optval, optlen);

            if (result == 0) {
                int len = optlen.get(ValueLayout.JAVA_INT, 0);
                if (len > 0) {
                    byte[] packet = optval.asSlice(0, len).toArray(ValueLayout.JAVA_BYTE);

                    int version = (packet[0] >> 4) & 0x0F;
                    int tcpOffset;

                    // Support Dual-Stack (IPv4 and IPv6)
                    if (version == 4) {
                        int ipHeaderLen = (packet[0] & 0x0F) * 4;
                        sig.ttl = Byte.toUnsignedInt(packet[8]);
                        tcpOffset = ipHeaderLen;
                    } else if (version == 6) {
                        sig.ttl = Byte.toUnsignedInt(packet[7]); // Hop Limit in IPv6
                        tcpOffset = 40; // IPv6 base header is always 40 bytes
                    } else {
                        return sig; // Unknown protocol
                    }

                    // TCP Header parsing
                    if (tcpOffset + 20 <= packet.length) {
                        int wSizeByte1 = Byte.toUnsignedInt(packet[tcpOffset + 14]);
                        int wSizeByte2 = Byte.toUnsignedInt(packet[tcpOffset + 15]);
                        sig.windowSize = (wSizeByte1 << 8) | wSizeByte2;

                        int tcpHeaderLen = ((Byte.toUnsignedInt(packet[tcpOffset + 12]) >> 4) & 0x0F) * 4;
                        int optionsLen = tcpHeaderLen - 20;

                        // Extract TCP Options Hex String
                        if (optionsLen > 0 && (tcpOffset + 20 + optionsLen) <= packet.length) {
                            int optIdx = 0;
                            int baseOffset = tcpOffset + 20;

                            // TLV Parser
                            while (optIdx < optionsLen) {
                                int kind = Byte.toUnsignedInt(packet[baseOffset + optIdx]);

                                if (kind == 0) {
                                    break; // EOL (End of Option List)
                                }
                                if (kind == 1) {
                                    optIdx++; // NOP (No-Operation, used for padding)
                                    continue;
                                }

                                // All other options have a length byte
                                if (optIdx + 1 >= optionsLen) break; // Malformed options safeguard
                                int optLen = Byte.toUnsignedInt(packet[baseOffset + optIdx + 1]);
                                if (optLen < 2) break; // Malformed length safeguard

                                switch (kind) {
                                    case 2: // MSS (Maximum Segment Size) - Length 4
                                        if (optLen == 4 && optIdx + 3 < optionsLen) {
                                            sig.mss = (Byte.toUnsignedInt(packet[baseOffset + optIdx + 2]) << 8) |
                                                      Byte.toUnsignedInt(packet[baseOffset + optIdx + 3]);
                                        }
                                        break;
                                    case 3: // Window Scale - Length 3
                                        if (optLen == 3 && optIdx + 2 < optionsLen) {
                                            sig.windowScale = Byte.toUnsignedInt(packet[baseOffset + optIdx + 2]);
                                        }
                                        break;
                                    case 4: // SACK Permitted - Length 2
                                        if (optLen == 2) {
                                            sig.sackPermitted = true;
                                        }
                                        break;
                                    // Case 8 is Timestamps (Length 10), usually not needed for basic bot detection
                                }
                                optIdx += optLen; // Jump to the next option
                            }

                        }
                    }
                }
            }
        } catch (Throwable t) {
            // FFM call failed (likely TCP_SAVE_SYN not set on server)
        }
        return sig;
    }

    public static final class ConnectionRecord {
        public final long acceptTimestamp = System.currentTimeMillis();
        private final int clientFd;
        public final String addr;
        public long handshakeTimestamp = -1;
        public int nextState = -1;
        ConnectionStats stats;
        TrafficHeuristics.Result evaluation;

        volatile boolean closed;

        public SynSignature synSignature;
        public final List<TcpSample> tcpSamples = new ArrayList<>();

        public ConnectionRecord(int clientFd, String addr) {
            this.clientFd = clientFd;
            this.addr = addr;
        }

        public long getL7DeltaT() {
            return handshakeTimestamp != -1 ? (handshakeTimestamp - acceptTimestamp) : -1;
        }

        long getAgeMs() {
            return System.currentTimeMillis() - this.acceptTimestamp;
        }
    }

    public static class SynSignature {
        public int ttl = -1;
        public int windowSize = -1;

        public int mss = -1;
        public int windowScale = -1;
        public boolean sackPermitted = false;

        @Override
        public String toString() {
            return "SynSignature{" +
                   "ttl=" + ttl +
                   ", windowSize=" + windowSize +
                   '}';
        }
    }

    public enum SampleTrigger {
        CONNECT,
        PERIODIC,
        L7_HANDSHAKE,
        L7_LOGIN_START,
        L7_STATUS_REQUEST,
        CHANNEL_INACTIVE,
        TIMED_OUT
    }

    public record TcpSample(
            int state,
            int caState,
            int retransmits,
            int probes,
            int backoff,
            int options,
            int sndWscale,
            int rcvWscale,
            long rto,
            long ato,
            long sndMss,
            long rcvMss,
            long unacked,
            long sacked,
            long lost,
            long retrans,
            long fackets,
            long lastDataSent,
            long lastAckSent,
            long lastDataRecv,
            long lastAckRecv,
            long pmtu,
            long rcvSsthresh,
            long rtt,
            long rttvar,
            long sndSsthresh,
            long sndCwnd,
            long advmss,
            long reordering,
            long rcvRtt,
            long rcvSpace,
            long totalRetrans,
            SampleTrigger trigger,
            long timestamp
    ) {
        public TcpSample(long[] info, SampleTrigger trigger) {
            this(
                    (int) info[0], (int) info[1], (int) info[2], (int) info[3],
                    (int) info[4], (int) info[5], (int) info[6], (int) info[7],
                    info[8], info[9], info[10], info[11], info[12], info[13],
                    info[14], info[15], info[16], info[17], info[18], info[19],
                    info[20], info[21], info[22], info[23], info[24], info[25],
                    info[26], info[27], info[28], info[29], info[30], info[31],
                    trigger, System.currentTimeMillis()
            );
        }
    }
}