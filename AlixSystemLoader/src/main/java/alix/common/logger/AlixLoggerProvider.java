package alix.common.logger;

public interface AlixLoggerProvider {

    LoggerAdapter getLoggerAdapter();

    String BRIGHT_RED = "\u001B[1;31m";
    String BRIGHT_CYAN = "\u001B[1;36m";
    String RESET = "\u001B[0m";

    String LOGGER_NAME_BRANCHLESS = BRIGHT_RED + "AlixSystem" + RESET;//"AlixSystem"
    String LOGGER_NAME_FULL = BRIGHT_RED + "[AlixSystem]" + RESET + " ";//"[AlixSystem] "

    static String wrap(String text, ConsoleColor color) {
        return color + text + RESET;
    }
}