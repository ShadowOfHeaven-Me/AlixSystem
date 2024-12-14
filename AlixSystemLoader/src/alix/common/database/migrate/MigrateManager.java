package alix.common.database.migrate;

import alix.common.database.DatabaseReader;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public final class MigrateManager {

    @SneakyThrows
    private static void migrateWith(DatabaseReader reader, MigrateSQLProvider migrateProvider, String query) {
        ResultSet rs = reader.getResult(query);

        while (rs.next()) migrateProvider.migrateEntry(rs);
    }

    public static void init() {

    }
}