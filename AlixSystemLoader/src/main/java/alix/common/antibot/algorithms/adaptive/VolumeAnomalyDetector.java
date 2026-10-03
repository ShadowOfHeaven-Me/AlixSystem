package alix.common.antibot.algorithms.adaptive;

final class VolumeAnomalyDetector {
    private final Ewma baseline;
    private final MADBaseline baselineJitter;
    private double cusum = 0;
    private final double slack;
    private final double elevatedAt;
    private final double attackAt;
    private final double minVolumeToInstantAttack;
    private State state = State.NORMAL;

    VolumeAnomalyDetector(double alpha, int windowSize, double slack, double elevatedAt, double attackAt, double minVolumeToInstantAttack) {
        this.baseline = new Ewma(alpha);
        this.baselineJitter = new MADBaseline(windowSize);
        this.slack = slack;
        this.elevatedAt = elevatedAt;
        this.attackAt = attackAt;
        this.minVolumeToInstantAttack = minVolumeToInstantAttack;
    }

    State onBucket(double current) {
        double base = baseline.get();
        double jitter = baselineJitter.sigma();
        double z = (current - base) / jitter;

        //capped, not just floored at 0 - without a ceiling, one big enough burst (e.g. a legitimate wave of
        //reconnects after maintenance ends) can drive cusum into the hundreds against a baseline that's
        //frozen the moment it leaves NORMAL (see below), so it then takes an unreasonable number of empty/
        //low buckets to decay back down - in practice it never recovers on a live server that's never
        //truly idle, latching this source as ATTACK/ELEVATED indefinitely instead of self-healing once the
        //burst passes. Capping bounds the worst-case recovery time regardless of how large the triggering
        //burst was, without weakening how fast a real sustained flood still trips attackAt in the first place.
        cusum = Math.min(attackAt * 2, Math.max(0, cusum + z - slack)); //slack ignores small drift, e.g. 0.5-1.0

        if (cusum > attackAt || z > 8 && current > this.minVolumeToInstantAttack) state = State.ATTACK;
        else if (cusum > elevatedAt) state = State.ELEVATED;
        else if (cusum == 0) state = State.NORMAL;

        // don't let attack traffic redefine "normal" - freeze baseline while non-normal
        if (state == State.NORMAL) {
            baseline.update(current);
            baselineJitter.add(current);
        }

        return state;
    }
}