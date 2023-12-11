package alix.common.logger.impl;

import alix.common.logger.LoggerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class JavaLoggerImpl implements LoggerAdapter {

    private final Logger logger;

    public JavaLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String info) {
        this.logger.info(info);
    }

    @Override
    public void warning(String warning) {
        this.logger.warning(warning);
    }

    @Override
    public void error(String error) {
        this.logger.severe(error);
    }

    @Override
    public void debug(String debug) {
        this.logger.log(Level.CONFIG, debug);
    }
}