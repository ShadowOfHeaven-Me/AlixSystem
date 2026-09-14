package alix.common.antibot.algorithms.adaptive;

import alix.common.AlixCommonMain;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.throwable.AlixError;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public final class AdaptiveAnomalyDetector {

    static {
        AlixScheduler.repeatAsync(AdaptiveAnomalyDetector::evictIdle, 10, TimeUnit.MINUTES);
        AlixScheduler.repeatAsync(AdaptiveAnomalyDetector::tick, 1, TimeUnit.SECONDS);
    }

    private static final VolumeAnomalyDetector GLOBAL_JOINS = new VolumeAnomalyDetector(0.02, 180, 0.5, 4, 8);
    private static final VolumeAnomalyDetector GLOBAL_EMPTY = new VolumeAnomalyDetector(0.02, 180, 0.5, 4, 8);
    private static final VolumeAnomalyDetector GLOBAL_INVALID = new VolumeAnomalyDetector(0.02, 180, 0.5, 4, 8);
    // allowed to react faster
    private static final VolumeAnomalyDetector GLOBAL_FIREWALLED = new VolumeAnomalyDetector(0.05, 120, 0.5, 3, 6);

    private static final LongAdder joinCounter = new LongAdder();
    private static final LongAdder emptyCounter = new LongAdder();
    private static final LongAdder invalidCounter = new LongAdder();
    private static final LongAdder firewalledCounter = new LongAdder();

    public static volatile State GLOBAL_STATE = State.NORMAL;

    public static void tick() {
        var old = GLOBAL_STATE;
        var current = tickGlobal0();
        GLOBAL_STATE = current.state();

        if (old == current.state()) return;

        AlixCommonMain.logInfo("Adaptive Anomaly Detector state transition: " + old + " -> " + current.state() + ". Reason: " + current.description());
    }

    private static DescribedState tickGlobal0() {
        var j = new DescribedState(GLOBAL_JOINS.onBucket(joinCounter.sumThenReset()), "Connection count");
        var e = new DescribedState(GLOBAL_EMPTY.onBucket(emptyCounter.sumThenReset()), "Empty connections");
        var in = new DescribedState(GLOBAL_INVALID.onBucket(invalidCounter.sumThenReset()), "Invalid packets");
        var fw = new DescribedState(GLOBAL_FIREWALLED.onBucket(firewalledCounter.sumThenReset()), "Firewall activity");
        return worst(worst(j, e), worst(in, fw));
    }

    public static void onInvalidPacket() {
        invalidCounter.increment();
    }

    public static void onFirewall() {
        firewalledCounter.increment();
    }

    private static final Map<InetAddress, SourceMetrics> PER_IP = new ConcurrentHashMap<>();
    private static final Map<Integer, SourceMetrics> IPV4_SUBNET = new ConcurrentHashMap<>(); // /24
    private static final Map<Long, SourceMetrics> IPV6_PREFIX = new ConcurrentHashMap<>(); // /48

    private static SourceMetrics newPerIpMetrics() {
        return new SourceMetrics(0.1, 30, 1.5, 8, 15, 10_000, 10);
    }

    private static SourceMetrics newSubnetMetrics() {
        return new SourceMetrics(0.05, 60, 1.0, 10, 18, 5_000, 10);
    }

    public static void onJoin(InetAddress addr) {
        joinCounter.increment();
        PER_IP.computeIfAbsent(addr, a -> newPerIpMetrics()).recordJoin();
        subnetFor(addr).recordJoin();
    }

    public static void onEmptyClose(InetAddress addr) {
        emptyCounter.increment();
        PER_IP.computeIfAbsent(addr, a -> newPerIpMetrics()).recordEmptyClose();
        subnetFor(addr).recordEmptyClose();
    }

    public static State sourceState(InetAddress addr) {
        SourceMetrics ip = PER_IP.get(addr);
        SourceMetrics sn = subnetFor(addr);
        State a = ip != null ? ip.state() : State.NORMAL;
        return worst(a, sn.state());
    }

    private static SourceMetrics subnetFor(InetAddress addr) {
        if (addr instanceof Inet4Address v4)
            return IPV4_SUBNET.computeIfAbsent(ipv4SubnetKey(v4), k -> newSubnetMetrics());

        if (addr instanceof Inet6Address v6)
            return IPV6_PREFIX.computeIfAbsent(ipv6PrefixKey(v6), k -> newSubnetMetrics());

        throw new AlixError("what " + addr);
    }

    private static int ipv4SubnetKey(Inet4Address addr) {
        byte[] b = addr.getAddress();
        return ((b[0] & 0xFF) << 16) | ((b[1] & 0xFF) << 8) | (b[2] & 0xFF); // top 24 bits
    }

    private static long ipv6PrefixKey(Inet6Address addr) {
        byte[] b = addr.getAddress();
        long prefix = 0;
        for (int i = 0; i < 6; i++) prefix = (prefix << 8) | (b[i] & 0xFF); // top 48 bits
        return prefix;
    }

    static void evictIdle() {
        long cutoff = System.currentTimeMillis() - 30 * 60_000L;
        PER_IP.values().removeIf(m -> m.lastSeenMillis() < cutoff);
        IPV4_SUBNET.values().removeIf(m -> m.lastSeenMillis() < cutoff);
        IPV6_PREFIX.values().removeIf(m -> m.lastSeenMillis() < cutoff);
    }

    static DescribedState worst(DescribedState a, DescribedState b) {
        return a.state().ordinal() >= b.state().ordinal() ? a : b;
    }

    static State worst(State a, State b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}