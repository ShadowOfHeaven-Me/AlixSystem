package alix.common;

import alix.common.logger.AlixLoggerProvider;
import alix.common.messages.language.AlixLanguage;
import alix.common.utils.AlixCommonHandler;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.config.ConfigProvider;

import static alix.common.messages.language.AlixLanguage.DEFAULT_LANGUAGE;
import static alix.common.messages.language.AlixLanguage.SUPPORTED_LANGUAGES;

public final class AlixCommonMain {

    public static final AlixMain MAIN_CLASS_INSTANCE = AlixCommonHandler.getMainClassInstance();
    public static final AlixLoggerProvider LOGGER_PROVIDER = AlixCommonHandler.getLoggerProvider();

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

    static final class DefaultParamImpl implements AlixMain.Params {

        static final DefaultParamImpl INSTANCE = new DefaultParamImpl();
        static final String DEFAULT_MESSAGES_FILE = "messages.properties";

        boolean messagesWarned;

        @Override
        public String messagesFileName() {
            String language = ConfigProvider.config.getString("language", DEFAULT_LANGUAGE).toLowerCase();
            if (!AlixLanguage.isSupported(language)) {
                if (!messagesWarned) {
                    this.messagesWarned = true;
                    AlixCommonMain.logWarning("Unsupported 'language' value '" + language + "' in config.yml, falling back to '" + DEFAULT_LANGUAGE + "'. Supported values: " + SUPPORTED_LANGUAGES);
                }
                language = DEFAULT_LANGUAGE;

            }
            return language.equals(DEFAULT_LANGUAGE) ? DEFAULT_MESSAGES_FILE : "langs/" + language + ".properties";
        }

        @Override
        public char messagesSeparator() {
            return '=';
        }
    }


    private AlixCommonMain() {
    }
}