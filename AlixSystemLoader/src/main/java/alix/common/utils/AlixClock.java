package alix.common.utils;

public final class AlixClock {

    public static long currentTimeMillis() {
        return System.nanoTime() / 1_000_000L;
    }
}