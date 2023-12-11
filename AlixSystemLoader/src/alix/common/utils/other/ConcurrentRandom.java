package alix.common.utils.other;

import java.util.concurrent.ThreadLocalRandom;

public final class ConcurrentRandom {

    private static final ConcurrentRandom instance = new ConcurrentRandom();

    public int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public static ConcurrentRandom getInstance() {
        return instance;
    }

    private ConcurrentRandom() {
    }
}