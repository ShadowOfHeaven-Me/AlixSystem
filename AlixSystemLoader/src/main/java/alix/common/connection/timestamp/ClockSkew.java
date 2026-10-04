package alix.common.connection.timestamp;

import alix.common.AlixCommonMain;
import alix.common.antibot.ip.IPUtils;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClockSkew {

    static final Map<Long, ClockSkewEstimator> MAP = new ConcurrentHashMap<>();

    @SneakyThrows
    public static void on_timestamp(long nanos, int addr, int port, long tsval) {
        long key = ((long) addr << 32) | port;

        var es = MAP.computeIfAbsent(key, sex -> new ClockSkewEstimator());

        es.addSample(nanos, tsval);

        double est = es.estimateFrequencyHz();
        double nominalHz = ClockSkewEstimator.snapNominalHz(est);
        double skew = es.skewPpm(nominalHz);

        Duration duration = Duration.ofNanos(nanos - es.startNanos);

        AlixCommonMain.logInfo("addr=" + InetAddress.getByAddress(IPUtils.ipv4ByteArray(addr)).getHostAddress() +
                               " nominalHz=" + nominalHz + " count=" + es.count + " duration= "+ duration + " estimateFrequencyHz=" + est + " skew=" + skew);
    }
}