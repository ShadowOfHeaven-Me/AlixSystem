package alix.common;

import alix.common.logger.AlixLoggerProvider;
import alix.common.utils.AlixCommonHandler;
import alix.common.utils.config.ConfigParams;

public final class AlixCommonMain {

    //public static final LoaderBootstrap BOOTSTRAP = AlixCommonHandler.getBootstrap();
    public static final AlixMain MAIN_CLASS_INSTANCE = AlixCommonHandler.getMainClassInstance();
    public static final AlixLoggerProvider LOGGER_PROVIDER = AlixCommonHandler.getLoggerProvider();

    //public static final Path DATA_FOLDER_PATH = AlixCommonHandler.

    public static void logInfo(String info) {
        LOGGER_PROVIDER.getLoggerAdapter().info(info);
    }

    public static void logWarning(String warning) {
        LOGGER_PROVIDER.getLoggerAdapter().warning(warning);
    }

    public static void logError(String error) {
        LOGGER_PROVIDER.getLoggerAdapter().error(error);
    }

    public static void logError(String error, Object... args) {
        LOGGER_PROVIDER.getLoggerAdapter().error(String.format(error, args));
    }

    public static void debug(String debug) {
        if (ConfigParams.isDebugEnabled) LOGGER_PROVIDER.getLoggerAdapter().debug(debug);
    }

    private AlixCommonMain() {
    }
}