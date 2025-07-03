package alix.common.database.migrate;

import alix.common.database.connect.DatabaseConnector;

import java.util.function.Supplier;

public enum MigrateType {

    AUTH_ME(DatabaseConnector.SQLite(), AuthMeSQLMigrateProvider::new, "SELECT * FROM %s");

    private final Supplier<DatabaseConnector> connector;
    private final Supplier<MigrateProvider> migrateProvider;
    private final String query;

    MigrateType(Supplier<DatabaseConnector> connector, Supplier<MigrateProvider> migrateProvider, String query) {
        this.connector = connector;
        this.migrateProvider = migrateProvider;
        this.query = query;
    }

    public MigrateProvider getMigrateProvider() {
        return migrateProvider.get();
    }

    public DatabaseConnector getConnector() {
        return connector.get();
    }

    public String getQuery() {
        return query;
    }
}