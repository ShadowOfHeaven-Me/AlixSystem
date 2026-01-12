package alix.common.database.migrate;

import alix.common.database.connect.DatabaseConnector;
import alix.common.database.file.DatabaseConfig;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class MigrateManager {

    @SneakyThrows
    private static void migrateWith0(DatabaseConnector reader, MigrateProvider migrateProvider, String query) {
        reader.connect();
        try (Connection conn = reader.obtainInterface();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) migrateProvider.migrateEntry(rs);
        } finally {
            reader.disconnect();
        }
    }

    public static void migrate(MigrateType type) {
        migrateWith0(type.getConnector(), type.getMigrateProvider(), type.getQuery().formatted(DatabaseConfig.MIGRATE.TABLE_NAME()));
    }
}