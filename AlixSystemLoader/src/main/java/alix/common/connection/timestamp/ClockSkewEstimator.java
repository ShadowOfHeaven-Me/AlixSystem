package alix.common.connection.timestamp;

import java.util.Arrays;

/**
 * Estimates the sender's TCP-timestamp tick rate from (local arrival time, TSval) pairs.
 * <p>
 * Model: tsval ~= f * (t - d) + c, where d >= d_min is the one-way delay. Delay can only make a
 * packet look *later*, so in (t, tsval) space every sample lies on or BELOW the true line
 * y = f*t + c'. The estimate is the tightest line that lies on or above every sample
 * (Moon, Skelly & Towsley's linear program). Its optimum is the edge of the upper convex hull
 * that spans the mean arrival time.
 * <p>
 * The hull is maintained online (arrival times are monotonic), so memory is bounded by the hull
 * size, and no assumed frequency is needed.
 * <p>
 * Feed it kernel/pcap receive timestamps if you can; user-space arrival times add scheduling and
 * GC noise that is much larger than network jitter.
 */
final class ClockSkewEstimator {

    private static final long _32_BITS = Integer.toUnsignedLong(~0);
    private double[] hx = new double[32]; // upper-hull vertices, x (seconds) ascending
    private double[] hy = new double[32]; // y = ticks since first sample
    private int hullSize;

    public long count;
    private double sumX;
    public long startNanos;
    private long firstTsval, lastTsVal;

    void addSample(long nowNanos, int tsval) {
        long ts = Integer.toUnsignedLong(tsval);
        long lastTsVal = this.lastTsVal;
        this.lastTsVal = ts;

        if (count == 0) {
            startNanos = nowNanos;
            firstTsval = ts;
        }
        double x = (nowNanos - startNanos) * 1e-9;
        double y = ts - firstTsval;
        count++;
        sumX += x;

        if (hullSize > 0 && x <= hx[hullSize - 1]) { // same instant: keep the higher tick
            if (y <= hy[hullSize - 1]) return;
            hullSize--;
        }
        while (hullSize >= 2 && cross(hullSize - 2, hullSize - 1, x, y) >= 0) hullSize--;
        if (hullSize == hx.length) {
            hx = Arrays.copyOf(hx, hullSize * 2);
            hy = Arrays.copyOf(hy, hullSize * 2);
        }
        hx[hullSize] = x;
        hy[hullSize++] = y;
    }

    private double cross(int a, int b, double cx, double cy) {
        return (hx[b] - hx[a]) * (cy - hy[a]) - (hy[b] - hy[a]) * (cx - hx[a]);
    }

    /**
     * Estimated tick rate in Hz (ticks per second of the LOCAL clock), or NaN if too few samples.
     */
    double estimateFrequencyHz() {
        if (hullSize < 2) return Double.NaN;
        double meanX = sumX / count;
        /*int i = 1;
        while (i < hullSize - 1 && hx[i] < meanX) i++;
        return (hy[i] - hy[i - 1]) / (hx[i] - hx[i - 1]);*/

        int lo = 1;
        int hi = hullSize - 1;

        while (lo < hi) {
            int mid = (lo + hi) >>> 1;

            if (hx[mid] < meanX) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }

        int i = lo;
        double dx = hx[i] - hx[i - 1];

        double hz = (hy[i] - hy[i - 1]) / dx;
        return hz;
    }

    private static final double[] NOMINAL_HZ = new double[]{100, 250, 300, 1000};

    static double snapNominalHz(double est) {
        double best = Double.NaN, bestRel = 0.5 / 100; // accept within 0.5%
        for (double c : NOMINAL_HZ) {
            double rel = Math.abs(est / c - 1);
            if (rel < bestRel) {
                bestRel = rel;
                best = c;
            }
        }
        //estimate the freq if none match, assumes <0.5 ppm accuracy of measurement
        return Double.isNaN(best) ? Math.round(est) : best;
    }

    /**
     * Relative skew in ppm against a nominal tick rate (100, 250, 1000 ...).
     */
    static double skewPpm(double estFreqHz, double nominalHz) {
        return (estFreqHz / nominalHz - 1.0) * 1e6;
    }

    /**
     * Observation window in seconds; skew accuracy is roughly (min-delay jitter) / span.
     */
    double spanSeconds() {
        return hullSize == 0 ? 0 : hx[hullSize - 1];
    }
}