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

package nanolimbo.alix.server;


import java.util.logging.Level;
import java.util.logging.Logger;

public final class Log {

    private static final Logger LOGGER = Logger.getLogger("AlixVirtualLimbo");

    private Log() {
    }

    public static void info(Object msg, Object... args) {
        LOGGER.info(String.format(msg.toString(), args));
    }

    public static void debug(Object msg, Object... args) {
        LOGGER.log(Level.CONFIG, String.format(msg.toString(), args));
    }

    public static void warning(Object msg, Object... args) {
        LOGGER.warning(String.format(msg.toString(), args));
    }

    public static void warning(Object msg, Throwable t, Object... args) {
        LOGGER.log(Level.WARNING, String.format(msg.toString(), args), t);
    }

    public static void error(Object msg, Object... args) {
        LOGGER.severe(String.format(msg.toString(), args));
    }

    public static void error(Object msg, Throwable t, Object... args) {
        LOGGER.log(Level.SEVERE, String.format(msg.toString(), args), t);
    }

    public static boolean isDebug() {
        return true;
    }
}
