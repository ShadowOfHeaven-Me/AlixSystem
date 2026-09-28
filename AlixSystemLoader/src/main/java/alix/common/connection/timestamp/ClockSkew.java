package alix.common.connection.timestamp;

import alix.common.AlixCommonMain;
import alix.common.antibot.ip.IPUtils;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClockSkew {

    static final Map<Long, ClockSkewEstimator> MAP = new ConcurrentHashMap<>();

    @SneakyThrows
    public static void on_timestamp(int addr, int port, long tsval) {
        long key = ((long) addr << 32) | port;

        var es = MAP.computeIfAbsent(key, sex -> new ClockSkewEstimator(1000));

        es.addSample(System.nanoTime(), tsval);

        AlixCommonMain.logInfo("addr=" + InetAddress.getByAddress(IPUtils.ipv4ByteArray(addr)).getHostAddress() + " estimateFrequency=" + es.estimateFrequency());
    }
}