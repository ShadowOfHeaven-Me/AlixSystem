package alix.common.antibot.algorithms.adaptive;

final class Ewma {

    //exponential weighted moving average (basically just establishing a baseline)

    //baseline(t) = alpha * new_value + (1 - alpha) * baseline(t-1)

    private final double alpha; //how important is the newest value (0-1)
    private double value;
    private boolean init;

    //1 / alpha = up to how many updates to take into account (approximately)
    Ewma(double alpha) {
        this.alpha = alpha;
    }

    void update(double x) {
        this.value = init ? alpha * x + (1 - alpha) * value : x;
        this.init = true;
    }

    double get() {
        return value;
    }
}