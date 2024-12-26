package alix.common.utils;

public final class AlixMathUtils {

    public static int roundUp(double a) {
        int b = (int) a;
        double rem = a - b;

        return rem > 0 ? b + 1 : b;
    }
}