package alix.common.database.connect;

import alix.common.database.ThrowableFunction;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public interface DatabaseConnector {

    void connect();

    void disconnect();

    Connection obtainInterface();

    <V> V runQuery(ThrowableFunction<V, Connection, SQLException> function);

    default PreparedStatement query(String query) {
        return this.runQuery(connection -> connection.prepareStatement(query));
    }

    @SneakyThrows
    default ResultSet result(String query) {
        return this.query(query).executeQuery();
    }

    DatabaseType getType();

    static Supplier<DatabaseConnector> SQLite() {
        return AbstractDatabaseConnector.supply(DatabaseSQLite.class);
    }

    static Supplier<DatabaseConnector> mySQL() {
        return AbstractDatabaseConnector.supply(DatabaseMySQL.class);
    }
}