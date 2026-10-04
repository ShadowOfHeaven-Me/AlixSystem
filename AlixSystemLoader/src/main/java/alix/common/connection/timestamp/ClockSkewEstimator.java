package alix.common.connection.timestamp;

import java.util.Arrays;

/**
 * Estimates the sender's TCP-timestamp tick rate from (local arrival time, TSval) pairs.
 *
 * Model: tsval ~= f * (t - d) + c, where d >= d_min is the one-way delay. Delay can only make a
 * packet look *later*, so in (t, tsval) space every sample lies on or BELOW the true line
 * y = f*t + c'. The estimate is the tightest line that lies on or above every sample
 * (Moon, Skelly & Towsley's linear program). Its optimum is the edge of the upper convex hull
 * that spans the mean arrival time.
 *
 * The hull is maintained online (arrival times are monotonic), so memory is bounded by the hull
 * size, and no assumed frequency is needed.
 *
 * Feed it kernel/pcap receive timestamps if you can; user-space arrival times add scheduling and
 * GC noise that is much larger than network jitter.
 */
final class ClockSkewEstimator {
    private double[] hx = new double[32]; // upper-hull vertices, x (seconds) ascending
    private double[] hy = new double[32]; // y = ticks since first sample
    private int hullSize;

    public long count;
    private double sumX;
    public long startNanos;
    private int firstTsval;

    void addSample(long nowNanos, long tsval) {
        int ts = (int) tsval; // TSval is 32-bit
        if (count == 0) {
            startNanos = nowNanos;
            firstTsval = ts;
        }
        double x = (nowNanos - startNanos) * 1e-9;
        // int subtraction wraps mod 2^32, giving a correct signed delta across the wrap point
        // (valid while the connection is shorter than 2^31 ticks, ~24 days at 1 kHz)
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

    /** Estimated tick rate in Hz (ticks per second of the LOCAL clock), or NaN if too few samples. */
    double estimateFrequencyHz() {
        if (hullSize < 2) return Double.NaN;
        double meanX = sumX / count;
        int i = 1;
        while (i < hullSize - 1 && hx[i] < meanX) i++;
        return (hy[i] - hy[i - 1]) / (hx[i] - hx[i - 1]);
    }

    static double snapNominalHz(double est) {
        double best = Double.NaN, bestRel = 0.005; // accept within 0.5%
        for (double c : new double[]{100, 250, 300, 1000}) { // extend with rates you observe
            double rel = Math.abs(est / c - 1);
            if (rel < bestRel) { bestRel = rel; best = c; }
        }
        return best; // NaN = unknown nominal rate: log it, don't guess
    }

    /** Relative skew in ppm against a nominal tick rate (100, 250, 1000 ...). */
    double skewPpm(double nominalHz) {
        return (estimateFrequencyHz() / nominalHz - 1.0) * 1e6;
    }

    /** Observation window in seconds; skew accuracy is roughly (min-delay jitter) / span. */
    double spanSeconds() {
        return hullSize == 0 ? 0 : hx[hullSize - 1];
    }
}