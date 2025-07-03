package alix.common.database.migrate;

import alix.common.database.connect.DatabaseConnector;
import alix.common.database.file.DatabaseConfig;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public final class MigrateManager {

    @SneakyThrows
    private static void migrateWith0(DatabaseConnector reader, MigrateProvider migrateProvider, String query) {
        reader.connect();

        ResultSet rs = reader.result(query);
        while (rs.next()) migrateProvider.migrateEntry(rs);
    }

    public static void migrate(MigrateType type) {
        migrateWith0(type.getConnector(), type.getMigrateProvider(), type.getQuery().formatted(DatabaseConfig.TABLE_NAME));
    }

    public static void init() {
    }
}