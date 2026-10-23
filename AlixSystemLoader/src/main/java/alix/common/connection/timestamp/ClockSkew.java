package alix.common.connection.timestamp;

import alix.common.antibot.ip.IPUtils;
import alix.common.utils.AlixCommonUtils;
import io.netty.channel.Channel;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;
import lombok.SneakyThrows;

import java.net.Inet4Address;

public final class ClockSkew {

    public static final boolean ENABLED = true;
    static final LongObjectMap<ClockSkewEstimator> MAP = new LongObjectHashMap<>();

    public static ClockSkewValue getClockSkew(Channel channel) {
        var es = MAP.get(key(channel));

        if (es == null) return new ClockSkewValue(Double.NaN, ClockSkewConfidence.NONE);

        double est = es.estimateFrequencyHz();
        double nominalHz = ClockSkewEstimator.snapNominalHz(est);
        double skew = ClockSkewEstimator.skewPpm(est, nominalHz);

        return new ClockSkewValue(skew, ClockSkewConfidence.of(es.spanSeconds()));
    }

    public static void on_disconnect(Channel channel) {
        MAP.remove(key(channel));
    }

    private static ClockSkewEstimator get(Channel channel) {
        return compute(key(channel));
    }

    private static long key(Channel channel) {
        var ip = AlixCommonUtils.getSocketAddress(channel);
        if (!(ip.getAddress() instanceof Inet4Address v4)) return 0;

        int addr = IPUtils.ipv4Value(v4);
        short port = (short) ip.getPort();

        return key(addr, port);
    }

    private static long key(int addr, short port) {
        return ((long) addr << 32) | port;
    }

    private static ClockSkewEstimator compute(long key) {
        return MAP.computeIfAbsent(key, sex -> new ClockSkewEstimator());
    }

    @SneakyThrows
    public static void on_timestamp(long nanos, int addr, short port, int tsval) {
        var es = compute(key(addr, port));
        es.addSample(nanos, tsval);

        /*double est = es.estimateFrequencyHz();
        double nominalHz = ClockSkewEstimator.snapNominalHz(est);
        double skew = ClockSkewEstimator.skewPpm(est, nominalHz);

        Duration duration = Duration.ofNanos(nanos - es.startNanos);

        AlixCommonMain.logInfo("addr=" + InetAddress.getByAddress(IPUtils.ipv4ByteArray(addr)).getHostAddress() +
                " nominalHz=" + nominalHz + " count=" + es.count + " duration= " + duration + " estimateFrequencyHz=" + est + " skew=" + skew);
    */
    }
}