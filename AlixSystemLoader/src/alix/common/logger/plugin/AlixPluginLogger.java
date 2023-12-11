package alix.common.logger.plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class AlixPluginLogger extends Logger {

    private static final String LOGGER_NAME = "\u001B[1;31m" + "AlixSystem" + "\u001B[0m";
    //private final String prefix;

    public AlixPluginLogger(Logger serverLogger) {
        super(LOGGER_NAME, null);
        this.setParent(serverLogger);
        this.setLevel(Level.ALL);
    }

/*    public final void log(LogRecord log) {
        log.setMessage(LOGGER_NAME + log.getMessage());
        super.log(log);
    }*/
}