package alix.common.logger;

import alix.common.environment.ServerEnvironment;
import alix.common.logger.impl.JavaLoggerImpl;
import alix.common.logger.plugin.BukkitAlixLogger;
import alix.common.logger.velocity.VelocityLoggerAdapter;
import alix.common.utils.other.throwable.AlixError;

import java.util.logging.Logger;

public interface LoggerAdapter {

    void log(String log);

    void info(String info);

    void warning(String warning);

    void error(String error);

    void debug(String debug);

    static LoggerAdapter from(String prefix) {
        return switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT, PAPER -> createAdapter(new BukkitAlixLogger(prefix));
            case VELOCITY -> new VelocityLoggerAdapter(prefix);
            default -> throw new AlixError("Unexpected value: " + ServerEnvironment.getEnvironment());
        };
    }

    static LoggerAdapter createAdapter(Logger logger) {
        return new JavaLoggerImpl(logger);
    }

    /*static LoggerAdapter createAdapter(org.slf4j.Logger logger) {
        return new Slf4jLoggerImpl(logger);
    }*/
}