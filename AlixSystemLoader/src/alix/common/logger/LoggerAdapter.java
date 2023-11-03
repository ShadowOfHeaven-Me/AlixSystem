package alix.common.logger;

import alix.common.logger.impl.JavaLoggerImpl;
import alix.common.logger.impl.Slf4jLoggerImpl;

import java.util.logging.Logger;

public interface LoggerAdapter {

    void info(String info);

    void warning(String warning);

    void error(String error);

    void debug(String debug);

    static LoggerAdapter createAdapter(Logger logger) {
        return new JavaLoggerImpl(logger);
    }

    static LoggerAdapter createAdapter(org.slf4j.Logger logger) {
        return new Slf4jLoggerImpl(logger);
    }
}