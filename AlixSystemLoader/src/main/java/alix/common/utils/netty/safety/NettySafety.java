package alix.common.utils.netty.safety;

import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.Log;

public final class NettySafety {

    public static final int MAX_RECEIVED_SIZE = 1 << 15;//32768
    private static final NettySafetyException INVALID_ALLOC = NettySafetyException.of("Invalid alloc");
    public static final NettySafetyException INVALID_VAR_INT = NettySafetyException.of("Invalid VarInt");
    public static final NettySafetyException INVALID_PACKET_LEN = NettySafetyException.of("NUH-UH");
    public static final NettySafetyException INVALID_BUF = NettySafetyException.of("Invalid buf");
    public static final NettySafetyException INVALID_STATE = NettySafetyException.of("Invalid state");
    public static final NettySafetyException INVALID_PACKET = NettySafetyException.of("Invalid packet");
    public static final NettySafetyException INVALID_UUID_RESP = NettySafetyException.of("Invalid UUID Resp");

    public static void validateUserInputBufAlloc(int allocSize) {
        if (allocSize > MAX_RECEIVED_SIZE || allocSize < 0) {
            if (!NanoLimbo.suppressInvalidPackets) Log.error("Alloc Size: " + allocSize);
            throw INVALID_ALLOC;
        }
    }
}