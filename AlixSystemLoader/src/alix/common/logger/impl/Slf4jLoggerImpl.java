package alix.common.logger.impl;

import alix.common.logger.LoggerAdapter;
import org.slf4j.Logger;

public class Slf4jLoggerImpl implements LoggerAdapter {

    private final Logger logger;

    public Slf4jLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String info) {
        this.logger.info(info);
    }

    @Override
    public void warning(String warning) {
        this.logger.warn(warning);
    }

    @Override
    public void error(String error) {
        this.logger.error(error);
    }

    @Override
    public void debug(String debug) {
        this.logger.debug(debug);
    }
}