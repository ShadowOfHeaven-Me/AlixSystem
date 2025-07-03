package alix.common.database.connect;

import alix.common.database.file.DatabaseConfig;

final class DatabaseSQLite extends AbstractDatabaseConnector {

    //Source code: https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/database/connector/AuthenticSQLiteDatabaseConnector.java#L21

    DatabaseSQLite() {
        hikariConfig.setPoolName("AlixSystem SQLite Pool");
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setMaxLifetime(DatabaseConfig.MAX_LIFETIME);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariConfig.setJdbcUrl("jdbc:sqlite:" + DatabaseConfig.SQLITE_FILE.getAbsolutePath().replace("\\", "/"));
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MY_SQL;
    }
}