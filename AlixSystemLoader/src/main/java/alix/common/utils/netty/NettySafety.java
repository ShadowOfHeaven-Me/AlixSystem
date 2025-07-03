package alix.common.utils.netty;

import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.Log;

public final class NettySafety {

    public static final int MAX_RECEIVED_SIZE = 1 << 15;//32768
    private static final InvalidAlloc EXC = new InvalidAlloc();

    public static void validateUserInputBufAlloc(int allocSize) {
        if (allocSize > MAX_RECEIVED_SIZE || allocSize < 0) {
            if (!NanoLimbo.suppressInvalidPackets) Log.error("Size: " + allocSize);
            throw EXC;
        }
    }

    private static final class InvalidAlloc extends RuntimeException {
    }
}