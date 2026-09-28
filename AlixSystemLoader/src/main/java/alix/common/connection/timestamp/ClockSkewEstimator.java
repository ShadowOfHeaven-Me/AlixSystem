package alix.common.connection.timestamp;

import java.util.Map;
import java.util.TreeMap;

final class ClockSkewEstimator {
    private static final long BUCKET_MS = 20_000;

    private final Map<Long, double[]> minOffsetPerBucket = new TreeMap<>(); // bucket -> {elapsed, relTsval}
    private long startNanos = -1;
    private long firstTsval = -1;
    private final double assumedHz; // rough guess, just used to rank samples by apparent delay

    ClockSkewEstimator(double assumedHz) {
        this.assumedHz = assumedHz;
    }

    void addSample(long nowNanos, long tsval) {
        if (startNanos < 0) {
            startNanos = nowNanos;
            firstTsval = tsval;
        }
        double elapsed = (nowNanos - startNanos) / 1e9;
        double relTsval = tsval - firstTsval;
        double offset = elapsed - relTsval / assumedHz; // smaller = less delay-corrupted

        long bucket = (nowNanos - startNanos) / (BUCKET_MS * 1_000_000L);
        minOffsetPerBucket.merge(bucket, new double[]{elapsed, relTsval}, (existing, incoming) -> {
            double existingOffset = existing[0] - existing[1] / assumedHz;
            double incomingOffset = incoming[0] - incoming[1] / assumedHz;
            return incomingOffset < existingOffset ? incoming : existing;
        });
    }

    double estimateFrequency() {
        var pts = minOffsetPerBucket.values();
        int n = pts.size();
        double sx = 0, sy = 0, sxy = 0, sxx = 0;
        for (double[] p : pts) {
            sx += p[0];
            sy += p[1];
            sxy += p[0] * p[1];
            sxx += p[0] * p[0];
        }
        return (n * sxy - sx * sy) / (n * sxx - sx * sx);
    }
}