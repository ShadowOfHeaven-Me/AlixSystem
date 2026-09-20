package alix.common.antibot.algorithms.adaptive;

import alix.common.connection.ratelimit.RateLimiter;

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

    RateLimiter<K> getRateLimiter() {
        return rateLimiter;
    }

    State recordConnectionEstablished() {
        connectionsBucketCount.increment();
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

        State j = joins.onBucket(connectionsCnt);
        State e = emptyCloses.onBucket(emptyCnt);

        this.bucketStart = now;
        this.bucketsProcessed++;

        if (this.bucketsProcessed < this.minBucketsToArm) {
            //even though connectionsCnt >= emptyCnt is supposed to hold true, if we ever change the params the buckets can technically be different
            this.state = this.heuristic(Math.max(connectionsCnt, emptyCnt));
            return;
        }

        this.state = j.worst(e);
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