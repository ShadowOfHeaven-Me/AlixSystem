package alix.common.antibot.algorithms.adaptive;

import alix.common.antibot.firewall.AlgorithmId;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.connection.ratelimit.RateLimiter;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.throwable.AlixError;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Logger;

public final class AdaptiveAnomalyDetector {

    static {
        AlixScheduler.repeatAsync(AdaptiveAnomalyDetector::evictIdle, 10, TimeUnit.MINUTES);
        AlixScheduler.repeatAsync(AdaptiveAnomalyDetector::tick, 1, TimeUnit.SECONDS);
    }

    private static final VolumeAnomalyDetector GLOBAL_JOINS = new VolumeAnomalyDetector(0.02, 180, 0.5, 4, 8, 50);
    private static final VolumeAnomalyDetector GLOBAL_EMPTY = new VolumeAnomalyDetector(0.03, 120, 0.5, 4, 8, 30);
    private static final VolumeAnomalyDetector GLOBAL_INVALID = new VolumeAnomalyDetector(0.02, 180, 0.5, 4, 8, 15);
    // allowed to react faster
    private static final VolumeAnomalyDetector GLOBAL_FIREWALLED = new VolumeAnomalyDetector(0.05, 120, 0.5, 3, 6, 10);

    private static final LongAdder connectionsEstablishedCounter = new LongAdder();
    private static final LongAdder emptyCounter = new LongAdder();
    private static final LongAdder invalidCounter = new LongAdder();
    private static final LongAdder firewalledCounter = new LongAdder();

    public static volatile State GLOBAL_STATE = State.NORMAL;

    private static final Logger LOGGER = Logger.getLogger("Alix - AdaptiveAnomalyDetector");

    public static void tick() {
        var old = GLOBAL_STATE;
        var current = tickGlobal0();
        var currentState = current.state();
        GLOBAL_STATE = currentState;

        if (old == currentState) return;

        if (currentState.isWorseThan(old)) {
            LOGGER.info("Global state changed: " + old + " -> " + currentState + ". Reason: " + current.description());
        } else
            LOGGER.info("Global state is calming down: " + old + " -> " + currentState);
    }

    private static DescribedState tickGlobal0() {
        var j = new DescribedState(GLOBAL_JOINS.onBucket(connectionsEstablishedCounter.sumThenReset()), "Connection count");
        var e = new DescribedState(GLOBAL_EMPTY.onBucket(emptyCounter.sumThenReset()), "Empty connections");
        var in = new DescribedState(GLOBAL_INVALID.onBucket(invalidCounter.sumThenReset()), "Invalid packets");
        var fw = new DescribedState(GLOBAL_FIREWALLED.onBucket(firewalledCounter.sumThenReset()), "Firewall activity");
        return j.worst(e).worst(in.worst(fw));
    }

    public static void onInvalidPacket() {
        invalidCounter.increment();
    }

    public static void onFirewall() {
        firewalledCounter.increment();
    }

    private static final Map<InetAddress, SourceMetrics<InetAddress>> PER_IP = new ConcurrentHashMap<>();
    private static final Map<Integer, SourceMetrics<Integer>> IPV4_SUBNET = new ConcurrentHashMap<>(); // /24
    private static final Map<Long, SourceMetrics<Long>> IPV6_PREFIX = new ConcurrentHashMap<>(); // /64

    //Kept in sync with TesterAdaptiveAnomalyDetector's own copy of these same constants (src/test/java) -
    //that harness can't call through this class directly, since referencing ANY member of
    //AdaptiveAnomalyDetector triggers its static initializer above, which schedules real background tasks
    //via AlixScheduler and needs a live Bukkit/Velocity platform on the classpath to do so.
    private static SourceMetrics<InetAddress> newPerIpMetrics() {
        return new SourceMetrics<>(0.1, 30, 1.5, 8, 15, 4_000,
                2, 3, 8, 20,
                new RateLimiter<>(3, 10));
    }

    private static <T> SourceMetrics<T> newSubnetMetrics() {
        return new SourceMetrics<>(0.05, 60, 2.0, 10, 18, 15_000,
                3, 7, 30, 200,
                new RateLimiter<>(10, 10));
    }

    //true if allowed
    private static <T> ConnectionVerdict handle(InetAddress addr, T subnetKey, State ipState, State subnetState, SourceMetrics<InetAddress> ipMetrics,
                                                SourceMetrics<T> subnetMetrics) {
        if (ipState == State.ATTACK) {
            FireWallManager.add(addr, AlgorithmId.K1, true);
            return ConnectionVerdict.FIREWALLED;
        }

        if (ipState == State.ELEVATED) {
            if (ipMetrics.getRateLimiter().tryAcquire(addr))
                return ConnectionVerdict.ALLOWED;

            return ConnectionVerdict.RATE_LIMITED;
        }

        if (subnetState == State.ELEVATED && !GeoIPTracker.isMapped(addr) || subnetState == State.ATTACK) {
            if (subnetMetrics.getRateLimiter().tryAcquire(subnetKey))
                return ConnectionVerdict.ALLOWED;

            return ConnectionVerdict.RATE_LIMITED;
        }

        return ConnectionVerdict.ALLOWED;
    }

    public static ConnectionVerdict onConnection(InetAddress addr) {
        return onConnection(addr, 1);
    }

    //weight lets a caller (LimboIntegration, via TelemetryProfiler.synSignature(channel)) make a single
    //connection count as more than 1 toward the CUSUM buckets below when its SYN fingerprint looks
    //suspicious. See SourceMetrics#recordConnectionEstablished(int).
    public static ConnectionVerdict onConnection(InetAddress addr, int weight) {
        connectionsEstablishedCounter.add(weight);
        var ipMetrics = PER_IP.computeIfAbsent(addr, a -> newPerIpMetrics());
        var ipState = ipMetrics.recordConnectionEstablished(weight);

        var subnetKey = subnetKey(addr);
        var subnetMetrics = computeSubnetForKey(subnetKey);
        var subnetState = subnetMetrics.recordConnectionEstablished(weight);

        return handle(addr, subnetKey, ipState, subnetState, ipMetrics, subnetMetrics);
    }

    public static void onEmptyClose(InetAddress addr) {
        emptyCounter.increment();
        var ipMetrics = PER_IP.computeIfAbsent(addr, a -> newPerIpMetrics());
        var ipState = ipMetrics.recordEmptyClose();

        var subnetKey = subnetKey(addr);
        var subnetMetrics = computeSubnetForKey(subnetKey);
        var subnetState = subnetMetrics.recordEmptyClose();

        handle(addr, subnetKey, ipState, subnetState, ipMetrics, subnetMetrics);
    }

    public static State sourceState(InetAddress addr) {
        SourceMetrics ip = PER_IP.get(addr);
        SourceMetrics sn = getSubnetFor(addr);
        State a = ip != null ? ip.state() : State.NORMAL;
        State b = sn != null ? sn.state() : State.NORMAL;
        return a.worst(b);
    }

    private static <T> T subnetKey(InetAddress addr) {
        if (addr instanceof Inet4Address v4)
            return (T) Integer.valueOf(ipv4SubnetKey(v4));

        if (addr instanceof Inet6Address v6)
            return (T) Long.valueOf(ipv6PrefixKey(v6));

        throw new AlixError("what " + addr);
    }

    private static SourceMetrics computeSubnetForKey(Object key) {
        if (key instanceof Integer v4)
            return IPV4_SUBNET.computeIfAbsent(v4, k -> newSubnetMetrics());

        if (key instanceof Long v6)
            return IPV6_PREFIX.computeIfAbsent(v6, k -> newSubnetMetrics());

        throw new AlixError("what " + key);
    }

    private static SourceMetrics getSubnetFor(InetAddress addr) {
        if (addr instanceof Inet4Address v4)
            return IPV4_SUBNET.get(ipv4SubnetKey(v4));

        if (addr instanceof Inet6Address v6)
            return IPV6_PREFIX.get(ipv6PrefixKey(v6));

        throw new AlixError("what " + addr);
    }

    private static int ipv4SubnetKey(Inet4Address addr) {
        byte[] b = addr.getAddress();
        return ((b[0] & 0xFF) << 16) | ((b[1] & 0xFF) << 8) | (b[2] & 0xFF); // top 24 bits
    }

    private static long ipv6PrefixKey(Inet6Address addr) {
        byte[] b = addr.getAddress();
        long prefix = 0;
        for (int i = 0; i < 8; i++) prefix = (prefix << 8) | (b[i] & 0xFF); // top 64 bits
        return prefix;
    }

    static void evictIdle() {
        long cutoff = System.currentTimeMillis() - 30 * 60_000L;
        PER_IP.values().removeIf(m -> m.lastSeenMillis() < cutoff);
        IPV4_SUBNET.values().removeIf(m -> m.lastSeenMillis() < cutoff);
        IPV6_PREFIX.values().removeIf(m -> m.lastSeenMillis() < cutoff);
    }
}