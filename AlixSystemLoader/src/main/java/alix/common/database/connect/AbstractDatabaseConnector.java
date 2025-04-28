package alix.common.database.connect;

import alix.common.database.DatabaseConnector;
import alix.common.database.ThrowableFunction;
import alix.common.utils.other.throwable.AlixException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;

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

    @SneakyThrows
    @Override
    public void connect() {
        dataSource = new HikariDataSource(hikariConfig);
        obtainInterface().close(); //Verify connection
        connected = true;
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