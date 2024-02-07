package alix.common.logger.plugin;

import alix.common.logger.AlixLoggerProvider;
import alix.loaders.bukkit.BukkitAlixMain;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class AlixPaperLogger extends Logger {

    //private final String prefix;

    public AlixPaperLogger() {
        super(AlixLoggerProvider.LOGGER_NAME_BRANCHLESS, null);
        this.setParent(BukkitAlixMain.instance.getServer().getLogger());
        this.setLevel(Level.ALL);
    }

/*    @Override
    public void log(LogRecord record) {
        //if(record.getLevel() == Level.WARNING)
        super.log(record);
    }*/

    /*    public final void log(LogRecord log) {
        log.setMessage(LOGGER_NAME + log.getMessage());
        super.log(log);
    }*/
}