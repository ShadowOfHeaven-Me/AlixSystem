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

package nanolimbo.alix;

import alix.common.utils.other.throwable.AlixException;
import io.netty.channel.Channel;
import nanolimbo.alix.integration.LimboIntegration;
import nanolimbo.alix.server.LimboServer;
import nanolimbo.alix.server.Log;

public final class NanoLimbo {

    //https://github.com/Nan1t/NanoLimbo

    public static LimboServer load(Channel serverChannel, LimboIntegration integration) {
        try {
            return new LimboServer(serverChannel, integration);
        } catch (Exception e) {
            Log.error("Cannot start server: ", e);
            throw new AlixException(e);
        }
    }
}
