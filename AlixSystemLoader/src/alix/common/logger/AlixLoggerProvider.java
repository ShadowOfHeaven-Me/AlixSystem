package alix.common.logger;

import alix.common.environment.ServerEnvironment;
import alix.common.logger.plugin.AlixPaperLogger;
import alix.common.logger.plugin.AlixSpigotLogger;

import java.util.logging.Logger;

public interface AlixLoggerProvider {

    LoggerAdapter getLoggerAdapter();

    static Logger createServerAdequateLogger() {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
                return new AlixSpigotLogger();
            case PAPER:
                return new AlixPaperLogger();
            default:
                throw new AssertionError();
        }
    }

    String LOGGER_NAME_BRANCHLESS = "\u001B[1;31m" + "AlixSystem" + "\u001B[0m";//"AlixSystem"
    String LOGGER_NAME_FULL = "\u001B[1;31m" + "[AlixSystem]" + "\u001B[0m ";//"[AlixSystem] "
}