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
        return recordConnectionEstablished(1);
    }

    //weight lets a caller count one connection as more than one toward this bucket's CUSUM input, e.g. for a
    //suspicious SYN fingerprint. A sustained burst of such connections trips ELEVATED/ATTACK faster than an
    //equal-sized burst of ordinary ones, while a single suspicious connection barely moves the needle -
    //isolated anomalous fingerprints are common, benign noise (VPNs, mobile carriers, corporate NAT).
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

    //test-only: TesterAdaptiveAnomalyDetector (src/test/java, same package) drives buckets synthetically,
    //bypassing the bucketMillis wall-clock wait this class normally enforces, so a tuning simulation
    //covering hours of real traffic runs instantly instead of actually sleeping through it. Reuses the
    //exact same cold-start-heuristic/CUSUM logic real traffic goes through - never called from production
    //code.
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