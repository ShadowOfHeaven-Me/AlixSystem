package alix.common.logger;

import java.util.logging.Logger;

public interface AlixLoggerProvider {

    LoggerAdapter getLoggerAdapter();

    static Logger createServerAdequateLogger(Logger original) {
        return original;
        //return new AlixUniversalLogger();
        /*switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
                return new BukkitAlixLogger();
            //return new AlixSpigotLogger();
            case PAPER:
                //return original;//paper is broken
                //return new AlixPaperLogger();
                try {
                    return AlixPaperLogger.createLogger();
                } catch (Throwable e) {
                    return original;
                }
            default:
                throw new AssertionError();
        }*/
    }

    String BRIGHT_RED = "\u001B[1;31m";
    String BRIGHT_CYAN = "\u001B[1;36m";
    String RESET = "\u001B[0m";

    String LOGGER_NAME_BRANCHLESS = BRIGHT_RED + "AlixSystem" + RESET;//"AlixSystem"
    String LOGGER_NAME_FULL = BRIGHT_RED + "[AlixSystem]" + RESET + " ";//"[AlixSystem] "

    static String wrap(String text, ConsoleColor color) {
        return color + text + RESET;
    }
}