/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo;

import alix.common.AlixCommonMain;
import alix.common.utils.other.annotation.DebugOnly;
import alix.common.utils.other.throwable.AlixError;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.protocol.snapshot.SnapshotEncodeStrategy;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class NanoLimbo {

    //https://github.com/Nan1t/NanoLimbo

    public static final boolean debugMode = false;

    //debugMode ? !val : val
    private static boolean of(boolean val) {
        return debugMode != val;
    }

    public static final SnapshotEncodeStrategy STRATEGY = SnapshotEncodeStrategy.RUNTIME_CACHE;
    //@DebugOnly
    public static final boolean suppressInvalidPackets = true;//of(true);
    public static final boolean debugCipher = false;//of(false);
    //@DebugOnly
    public static final boolean debugPackets = of(false);
    public static final boolean debugServerPackets = false;//of(false);
    public static final boolean debugRawEncodes = false;//of(false);
    public static final boolean debugBytes = false;//of(false);
    public static final boolean debugFrames = false;
    public static final boolean debugSnapshots = false;//of(false);
    public static final boolean usePacketSnapshots = STRATEGY != SnapshotEncodeStrategy.NO_CACHE;
    //@DebugOnly
    public static final boolean pregenerateSnapshots = STRATEGY == SnapshotEncodeStrategy.PREGENERATE;//true
    public static final boolean allowFreeMovement = false;
    //@DebugOnly
    public static final boolean performChecks = true;
    //@DebugOnly
    public static final boolean debugAllDisconnects = false;//of(false);
    //@DebugOnly
    public static final boolean validateWrites = of(false);
    //@DebugOnly
    public static final boolean enableFingerprinting = false;
    //@DebugOnly
    public static final boolean logPos = false;//of(false);
    public static final boolean removeTimeout = true;
    public static final boolean centerSpawn = false;
    public static final boolean printCaptchaFailed = of(false);
    public static final boolean verifyTheDud = true;
    public static final boolean useTransfer = true;

    public static LimboServer LIMBO;
    public static LimboIntegration INTEGRATION;

    private static void verifyNotDebug0() throws IllegalAccessException {
        for (var f : NanoLimbo.class.getDeclaredFields()) {
            if (debugMode || f.isAnnotationPresent(DebugOnly.class)) {
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
        return suppressInvalidPackets && !(t instanceof AlixError);
    }
}