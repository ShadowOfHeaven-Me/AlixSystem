package alix.common.utils;

public final class AlixMathUtils {

    public static int clamp(int num, int min, int max) {
        return num < min ? min : Math.min(num, max);
    }

    public static int roundUp(double a) {
        int b = (int) a;
        double rem = a - b;

        return rem > 0 ? b + 1 : b;
    }

    public static double round(double f, int decimals) {
        int pow = 1;
        for (int i = 0; i < decimals; i++) pow *= 10;

        double l = (long) (f * pow);
        return l / pow;
    }

    public static float round(float f, int decimals) {
        int pow = 1;
        for (int i = 0; i < decimals; i++) pow *= 10;

        float l = (long) (f * pow);
        return l / pow;
    }
}