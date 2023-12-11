package alix.common.logger.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class LoggerMethodHolder {

    private final Logger logger;

    public LoggerMethodHolder(Logger logger) {
        this.logger = logger;
    }

    @NotNull
    public Logger getLogger() {
        return this.logger;
    }
}