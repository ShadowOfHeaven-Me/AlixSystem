package alix.common.logger.plugin;

import alix.common.logger.AlixLoggerProvider;
import alix.loaders.bukkit.BukkitAlixMain;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class AlixUniversalLogger extends Logger {

    private final String name = AlixLoggerProvider.LOGGER_NAME_FULL;

    public AlixUniversalLogger() {
        super("AlixSystem", null);
        this.setParent(BukkitAlixMain.instance.getServer().getLogger());
        this.setLevel(Level.ALL);
    }

    @Override
    public final void log(LogRecord record) {
        record.setMessage(this.name + record.getMessage());
        super.log(record);
    }
}