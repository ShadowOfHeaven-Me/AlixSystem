package alix.common.database.connect;

import alix.common.database.ThrowableFunction;
import alix.common.utils.other.throwable.AlixException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

abstract class AbstractDatabaseConnector implements DatabaseConnector {

    final HikariConfig hikariConfig = new HikariConfig();
    volatile HikariDataSource dataSource;
    private volatile boolean connected;

    @Override
    public <V> V runQuery(ThrowableFunction<V, Connection, SQLException> function) throws IllegalStateException {
        try {
            try (var connection = obtainInterface()) {
                return function.apply(connection);
            }
        } catch (SQLTransientConnectionException e) {
            throw new AlixException(e, "Lost connection to the database");
        } catch (SQLException e) {
            throw new AlixException(e);
        }
    }

    private static final Map<Class<? extends DatabaseConnector>, DatabaseConnector> CONNECTORS = new ConcurrentHashMap<>(4, 1.0f);

    @SneakyThrows
    private static <T extends DatabaseConnector> T newInstance(Class<T> clazz) {
        return clazz.getDeclaredConstructor().newInstance();
    }

    static <T extends DatabaseConnector> Supplier<DatabaseConnector> supply(Class<T> clazz) {
        return () -> CONNECTORS.computeIfAbsent(clazz, AbstractDatabaseConnector::newInstance);
    }

    @SneakyThrows
    @Override
    public void connect() {
        dataSource = new HikariDataSource(hikariConfig);
        connected = true;
        obtainInterface().close(); //Verify connection
    }

    @Override
    public void disconnect() {
        connected = false;
        dataSource.close();
    }

    @SneakyThrows
    @Override
    public Connection obtainInterface() {
        if (!connected) throw new AlixException("Not connected to the database!");
        return dataSource.getConnection();
    }
}