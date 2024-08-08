package alix.common.logger.plugin;

import alix.common.logger.AlixLoggerProvider;
import alix.common.reflection.CommonReflection;
import alix.loaders.bukkit.BukkitAlixMain;
import org.bukkit.plugin.PluginLogger;

import java.util.logging.Level;

public final class BukkitAlixLogger extends PluginLogger {

    private final String name = AlixLoggerProvider.LOGGER_NAME_FULL;

    public BukkitAlixLogger() {
        super(BukkitAlixMain.instance);
        this.setParent(BukkitAlixMain.instance.getServer().getLogger());
        this.setLevel(Level.ALL);
        CommonReflection.set(CommonReflection.getFieldAccessibleByType(PluginLogger.class, String.class), this, this.name);
        //PluginLogger.class.getFields();
    }

/*    @Override
    public void log(LogRecord record) {
        record.setMessage(this.name + record.getMessage());
        super.log(record);
    }*/
}