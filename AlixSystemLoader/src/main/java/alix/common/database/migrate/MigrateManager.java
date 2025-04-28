package alix.common.database.migrate;

import alix.common.database.DatabaseConnector;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public final class MigrateManager {

    @SneakyThrows
    private static void migrateWith0(DatabaseConnector reader, MigrateProvider migrateProvider, String query) {
        ResultSet rs = reader.result(query);

        while (rs.next()) migrateProvider.migrateEntry(rs);
    }

    private static void migrate_AuthMe_MySQL() {

    }

    public static void init() {
    }
}