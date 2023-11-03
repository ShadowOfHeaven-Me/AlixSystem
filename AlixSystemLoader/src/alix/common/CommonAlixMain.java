package alix.common;

import alix.common.logger.AlixLoggerProvider;
import alix.common.utils.file.FileManager;
import alix.pluginloader.LoaderBootstrap;

public class CommonAlixMain {

    public static LoaderBootstrap plugin;
    public static AlixLoggerProvider loggerManager;

    public static void logInfo(String info) {
        loggerManager.getLoggerAdapter().info(info);
    }

    public static void logWarning(String warning) {
        loggerManager.getLoggerAdapter().warning(warning);
    }

    public static void logError(String error) {
        loggerManager.getLoggerAdapter().error(error);
    }

}