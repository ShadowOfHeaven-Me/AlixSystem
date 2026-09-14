package alix.common.antibot.algorithms.adaptive;

import java.util.concurrent.atomic.LongAdder;

final class SourceMetrics {

    private final VolumeAnomalyDetector joins;
    private final VolumeAnomalyDetector emptyCloses;
    private final long bucketMillis;
    private final int minBucketsToArm;

    private final LongAdder joinBucketCount = new LongAdder();
    private final LongAdder emptyBucketCount = new LongAdder();
    private volatile long bucketStart = System.currentTimeMillis();
    private volatile long lastSeen = System.currentTimeMillis();
    private volatile int bucketsProcessed = 0;
    private volatile State state = State.NORMAL;

    SourceMetrics(double alpha, int window, double k, double elevatedAt, double attackAt,
                  long bucketMillis, int minBucketsToArm) {
        this.joins = new VolumeAnomalyDetector(alpha, window, k, elevatedAt, attackAt);
        this.emptyCloses = new VolumeAnomalyDetector(alpha, window, k, elevatedAt, attackAt);
        this.bucketMillis = bucketMillis;
        this.minBucketsToArm = minBucketsToArm;
    }

    void recordJoin() {
        joinBucketCount.increment();
        touch();
    }

    void recordEmptyClose() {
        emptyBucketCount.increment();
        touch();
    }

    private void touch() {
        lastSeen = System.currentTimeMillis();
        rolloverIfDue();
    }

    private synchronized void rolloverIfDue() {
        long now = System.currentTimeMillis();
        if (now - bucketStart < bucketMillis) return;

        State j = joins.onBucket(joinBucketCount.sumThenReset());
        State e = emptyCloses.onBucket(emptyBucketCount.sumThenReset());

        this.bucketStart = now;
        this.bucketsProcessed++;

        // do not take small samples into account
        this.state = (bucketsProcessed >= minBucketsToArm) ? AdaptiveAnomalyDetector.worst(j, e) : State.NORMAL;
    }

    State state() {
        return state;
    }

    long lastSeenMillis() {
        return lastSeen;
    }
}