package ua.nanit.limbo;

import alix.common.AlixCommonMain;
import alix.common.utils.netty.safety.NettySafetyException;
import alix.common.utils.other.annotation.DebugOnly;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.protocol.snapshot.SnapshotEncodeStrategy;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class NanoLimbo {

    //https://github.com/Nan1t/NanoLimbo

    //keep this val first, per verifyNotDebug0()
    public static final boolean debugMode = false;

    //debugMode ? !val : val
    private static boolean of(boolean val) {
        return debugMode != val;
    }

    private static final SnapshotEncodeStrategy STRATEGY = SnapshotEncodeStrategy.RUNTIME_CACHE;
    //@DebugOnly
    @DebugOnly(false)
    public static final boolean broadcastInvalidPacketFireWallStackTraces = false;
    @DebugOnly(true)
    public static final boolean suppressInvalidPackets = true;//of(true);
    @DebugOnly(false)
    public static final boolean debugCipher = false;//of(false);
    @DebugOnly(false)
    public static final boolean debugPackets = of(false);
    @DebugOnly(false)
    public static final boolean debugPacketSizes = false;
    @DebugOnly(false)
    public static final boolean debugServerPackets = false;//of(false);
    @DebugOnly(false)
    public static final boolean debugRawEncodes = false;//of(false);
    @DebugOnly(false)
    public static final boolean debugBytes = false;//of(false);
    @DebugOnly(false)
    public static final boolean debugFrames = false;
    @DebugOnly(false)
    public static final boolean debugSnapshots = false;//of(false);
    public static final boolean usePacketSnapshots = STRATEGY != SnapshotEncodeStrategy.NO_CACHE;
    public static final boolean pregenerateSnapshots = STRATEGY == SnapshotEncodeStrategy.PREGENERATE;//true

    @DebugOnly(false)
    public static final boolean allowFreeMovement = false;
    @DebugOnly(true)
    public static final boolean performChecks = true;
    @DebugOnly(false)
    public static final boolean debugAllDisconnects = of(false);
    @DebugOnly(false)
    public static final boolean validateWrites = of(false);
    @DebugOnly(false)
    public static final boolean enableFingerprinting = false;
    @DebugOnly(false)
    public static final boolean logPos = false;//of(false);
    @DebugOnly(true)
    public static final boolean removeTimeout = true;
    @DebugOnly(false)
    public static final boolean centerSpawn = false;
    @DebugOnly(false)
    public static final boolean printCaptchaFailed = of(false);
    @DebugOnly(true)
    public static final boolean verifyTheDud = true;
    @DebugOnly(true)
    public static final boolean useTransfer = true;

    public static LimboServer LIMBO;
    public static LimboIntegration INTEGRATION;

    private static void verifyNotDebug0() throws IllegalAccessException {
        for (var f : NanoLimbo.class.getDeclaredFields()) {
            boolean debug = debugMode;

            if (f.isAnnotationPresent(DebugOnly.class) && f.getType() == boolean.class) {
                DebugOnly ann = f.getAnnotation(DebugOnly.class);
                if (ann.value() != f.getBoolean(null)) debug = true;
            }

            if (debug) {
                AlixCommonMain.logWarning("Debug Mode is enabled, per " + f.getName() + "=" + f.get(null) + " annotated as DebugOnly! Contact the developer if you weren't expecting debug to be enabled!");
                return;
            }
        }
    }

    public static LimboServer load(LimboIntegration integration) {
        try {
            verifyNotDebug0();
            return new LimboServer(integration);
        } catch (Exception e) {
            Log.error("Cannot start virtual server: ", e);
            throw new AlixError(e);
        }
    }

    public static boolean suppress(Throwable t) {
        if (t instanceof NettySafetyException ex)
            throw ex;

        //From a ByteBuf
        if (t instanceof IndexOutOfBoundsException ex)
            throw ex;

        return suppressInvalidPackets && !(t instanceof AlixError) && !(t instanceof AlixException);
    }
}