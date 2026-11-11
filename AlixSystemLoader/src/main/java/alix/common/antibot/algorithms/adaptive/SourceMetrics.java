package alix.common.antibot.algorithms.adaptive;

import alix.common.connection.ratelimit.RateLimiter;

import java.net.InetAddress;
import java.util.concurrent.atomic.LongAdder;

final class SourceMetrics<K> {

    private final VolumeAnomalyDetector joins;
    private final VolumeAnomalyDetector emptyCloses;
    private final long bucketMillis;
    private final int minBucketsToArm;

    private final LongAdder connectionsBucketCount = new LongAdder();
    private final LongAdder emptyBucketCount = new LongAdder();
    private final double heuristicElevated;
    private final double heuristicAttack;
    private final RateLimiter<K> rateLimiter;
    private volatile long bucketStart = System.currentTimeMillis();
    private volatile long lastSeen = System.currentTimeMillis();
    private volatile int bucketsProcessed = 0;
    private volatile State state = State.NORMAL;

    SourceMetrics(double alpha, int window, double slack, double elevatedAt, double attackAt,
                  long bucketMillis, int minBucketsToArm, double heuristicElevated, double heuristicAttack,
                  double minVolumeToInstantAttack, RateLimiter<K> rateLimiter) {
        this.heuristicElevated = heuristicElevated;
        this.heuristicAttack = heuristicAttack;
        this.joins = new VolumeAnomalyDetector(alpha, window, slack, elevatedAt, attackAt, minVolumeToInstantAttack);
        this.emptyCloses = new VolumeAnomalyDetector(alpha, window, slack, elevatedAt, attackAt, minVolumeToInstantAttack);
        this.bucketMillis = bucketMillis;
        this.minBucketsToArm = minBucketsToArm;
        this.rateLimiter = rateLimiter;
    }
    static SourceMetrics<InetAddress> newPerIpMetrics() {
        return new SourceMetrics<>(0.1, 30, 1.5, 8, 15, 4_000,
                2, 3, 8, 20,
                new RateLimiter<>(3, 10));
    }

    static <T> SourceMetrics<T> newSubnetMetrics() {
        return new SourceMetrics<>(0.05, 60, 2.0, 10, 18, 15_000,
                3, 7, 30, 200,
                new RateLimiter<>(10, 10));
    }

    RateLimiter<K> getRateLimiter() {
        return rateLimiter;
    }

    State recordConnectionEstablished() {
        return recordConnectionEstablished(1);
    }

    State recordConnectionEstablished(int weight) {
        connectionsBucketCount.add(weight);
        touch();
        return this.state;
    }

    State recordEmptyClose() {
        emptyBucketCount.increment();
        touch();
        return this.state;
    }

    private void touch() {
        lastSeen = System.currentTimeMillis();
        rolloverIfDue();
    }

    private State heuristic(long cnt) {
        double rate = cnt / this.bucketSeconds();
        if (rate > this.heuristicAttack)
            return State.ATTACK;
        if (rate > this.heuristicElevated)
            return State.ELEVATED;

        return State.NORMAL;
    }

    private synchronized void rolloverIfDue() {
        long now = System.currentTimeMillis();
        if (now - bucketStart < bucketMillis) return;

        long connectionsCnt = connectionsBucketCount.sumThenReset();
        long emptyCnt = emptyBucketCount.sumThenReset();
        this.bucketStart = now;

        processBucket(connectionsCnt, emptyCnt);
    }

    private synchronized State processBucket(long connectionsCnt, long emptyCnt) {
        State j = joins.onBucket(connectionsCnt);
        State e = emptyCloses.onBucket(emptyCnt);

        this.bucketsProcessed++;

        if (this.bucketsProcessed < this.minBucketsToArm) {
            //even though connectionsCnt >= emptyCnt is supposed to hold true, if we ever change the params the buckets can technically be different
            this.state = this.heuristic(Math.max(connectionsCnt, emptyCnt));
        } else {
            this.state = j.worst(e);
        }
        return this.state;
    }

    //test-only
    State forceBucketForTest(long connectionsCnt, long emptyCnt) {
        return processBucket(connectionsCnt, emptyCnt);
    }

    private double bucketSeconds() {
        return this.bucketMillis / 1000d;
    }

    State state() {
        return state;
    }

    long lastSeenMillis() {
        return lastSeen;
    }
}