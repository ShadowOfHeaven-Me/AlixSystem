package alix.common.database;

import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface DatabaseReader {

    PreparedStatement runQuery(String query);

    @SneakyThrows
    default ResultSet getResult(String query) {
        return this.runQuery(query).executeQuery();
    }
}