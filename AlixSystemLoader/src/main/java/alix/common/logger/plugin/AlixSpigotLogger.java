package alix.common.logger.plugin;

import alix.common.logger.AlixLoggerProvider;
import alix.loaders.bukkit.BukkitAlixMain;
import org.bukkit.plugin.PluginLogger;

import java.lang.reflect.Field;
import java.util.ArrayList;

public final class AlixSpigotLogger extends PluginLogger {

    public AlixSpigotLogger() {
        super(BukkitAlixMain.instance);
        Field f2 = null;
        ArrayList<String> debug = new ArrayList<>();
        for (Field f : PluginLogger.class.getDeclaredFields()) {
            f.setAccessible(true);
            debug.add(f.getName() + " " + f.getType());
            if (f.getType() == String.class) {
                f2 = f;
                break;
            }
        }
        if (f2 == null) throw new ExceptionInInitializerError("Broken. Debug: " + debug.toString());

        try {
            f2.set(this, AlixLoggerProvider.LOGGER_NAME_FULL);
        } catch (IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}