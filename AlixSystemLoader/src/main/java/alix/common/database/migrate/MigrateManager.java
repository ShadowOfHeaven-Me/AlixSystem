package alix.common.database.migrate;

import alix.common.database.connect.DatabaseConnector;
import alix.common.database.file.DatabaseConfig;
import alix.common.utils.other.throwable.AlixError;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;

public final class MigrateManager {

    //FUNCTIONALITY (audit, 2026-09-24): a SQL identifier (table name) can't be parameterized via a
    //PreparedStatement '?' placeholder - JDBC only supports that for VALUES, not identifiers - so the
    //migrate-table-name value from database.yml is spliced directly into the query text below via
    //String#formatted(). Without validating it first, an admin-editable config value becomes a SQL
    //injection primitive (e.g. "authme; DROP TABLE alix_users2; --") on any setup where a lower-trust user
    //can edit plugin configs (shared/panel hosting). Table/column names are conventionally
    //alphanumeric+underscore, so an allow-list pattern is a safe, non-breaking restriction.
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

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
        String tableName = DatabaseConfig.MIGRATE.TABLE_NAME();
        if (!SAFE_IDENTIFIER.matcher(tableName).matches())
            throw new AlixError("Invalid migrate-table-name in database.yml: \"" + tableName + "\" - only letters, digits and underscores are allowed!");

        migrateWith0(type.getConnector(), type.getMigrateProvider(), type.getQuery().formatted(tableName));
    }
}