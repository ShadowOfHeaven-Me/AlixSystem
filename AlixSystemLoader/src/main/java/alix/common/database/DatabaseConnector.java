package alix.common.database;

import alix.common.database.connect.DatabaseType;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

}