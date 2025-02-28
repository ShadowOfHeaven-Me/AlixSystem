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

import alix.common.utils.other.annotation.DebugOnly;
import alix.common.utils.other.throwable.AlixException;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class NanoLimbo {

    //https://github.com/Nan1t/NanoLimbo

    private static final boolean debugMode = of(false);

    private static boolean of(boolean val) {
        return val;
    }

    public static final boolean debugCipher = of(false);
    @DebugOnly
    public static final boolean debugPackets = of(true);
    public static final boolean debugSnapshots = of(false);
    @DebugOnly
    public static final boolean allowFreeMovement = of(true);
    @DebugOnly
    public static final boolean performChecks = of(false);
    //@DebugOnly
    public static final boolean logPos = of(false);
    public static final boolean removeTimeout = of(true);
    public static final boolean centerSpawn = of(false);
    @DebugOnly
    public static final boolean printCaptchaFailed = of(true);
    public static final boolean verifyTheDud = of(true);

    public static LimboServer LIMBO;
    public static LimboIntegration INTEGRATION;

    public static LimboServer load(LimboIntegration integration) {
        try {
            return new LimboServer(integration);
        } catch (Exception e) {
            Log.error("Cannot start server: ", e);
            throw new AlixException(e);
        }
    }
}