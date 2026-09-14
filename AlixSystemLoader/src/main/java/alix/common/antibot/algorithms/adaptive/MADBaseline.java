package alix.common.antibot.algorithms.adaptive;

import java.util.Arrays;

final class MADBaseline {

    //Simply using MAD instead of stddev here to negate really short-term bursts

    private final double[] window;
    private int idx, count;

    MADBaseline(int size) {
        window = new double[size];
    }

    void add(double x) {
        window[idx] = x;
        idx = (idx + 1) % window.length;
        count = Math.min(count + 1, window.length);
    }

    double median() {
        double[] c = Arrays.copyOf(window, count);
        Arrays.sort(c);
        return c[c.length / 2];
    }

    double sigma() {
        if (count == 0) return 1.0;
        double med = median();
        double[] c = new double[count];
        for (int i = 0; i < count; i++) c[i] = Math.abs(window[i] - med);
        Arrays.sort(c);
        double mad = c[c.length / 2];
        return Math.max(1.0, 1.4826 * mad); // 1.4826 makes MAD comparable to a real stddev
    }
}