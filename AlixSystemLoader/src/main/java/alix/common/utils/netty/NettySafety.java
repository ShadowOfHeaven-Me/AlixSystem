package alix.common.utils.netty;

import alix.common.utils.other.throwable.AlixException;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.Log;

public final class NettySafety {

    public static final int MAX_RECEIVED_SIZE = 1 << 15;//32768
    private static final AlixException INVALID_ALLOC = new AlixException("Invalid alloc");
    public static final AlixException INVALID_VAR_INT = new AlixException("Invalid VarInt");
    public static final AlixException INVALID_PACKET_LEN = new AlixException("NUH-UH");
    public static final AlixException INVALID_BUF = new AlixException("Invalid buf");

    public static void validateUserInputBufAlloc(int allocSize) {
        if (allocSize > MAX_RECEIVED_SIZE || allocSize < 0) {
            if (!NanoLimbo.suppressInvalidPackets) Log.error("Size: " + allocSize);
            throw INVALID_ALLOC;
        }
    }
}