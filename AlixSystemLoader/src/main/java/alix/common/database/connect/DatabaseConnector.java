package alix.common.database.connect;

import alix.common.database.ThrowableConsumer;
import alix.common.database.ThrowableFunction;
import alix.common.database.file.DatabaseConfig;

import java.sql.Connection;
import java.util.function.Supplier;

public interface DatabaseConnector {

    void connect();

    void disconnect();

    Connection obtainInterface();

    @SuppressWarnings("UnusedReturnValue")
    <V> V getQuery(ThrowableFunction<V, Connection, Exception> function);

    void query(ThrowableConsumer<Connection, Exception> function);

    /*default PreparedStatement query(String query) {
        return this.runQuery(connection -> connection.prepareStatement(query));
    }

    @SneakyThrows
    default ResultSet result(String query) {
        return this.query(query).executeQuery();
    }*/

    DatabaseType getType();

    static Supplier<DatabaseConnector> SQLite(boolean migrate) {
        return () -> new DatabaseSQLite(DatabaseConfig.config(migrate));
    }

    static Supplier<DatabaseConnector> mySQL(boolean migrate) {
        return () -> new DatabaseMySQL(DatabaseConfig.config(migrate));
    }
}