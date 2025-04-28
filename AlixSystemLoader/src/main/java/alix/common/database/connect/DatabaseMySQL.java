package alix.common.database.connect;

import alix.common.database.file.DatabaseConfig;

final class DatabaseMySQL extends AbstractDatabaseConnector {

    //Source code: https://github.com/kyngs/LibreLogin/blob/5cae5bd01fa37e45ee1d529bfa5c8eb1dcdc5c58/Plugin/src/main/java/xyz/kyngs/librelogin/common/database/connector/AuthenticMySQLDatabaseConnector.java

    private static final String JDBC_URL = "jdbc:mariadb://%host%:%port%/%database%?autoReconnect=true&zeroDateTimeBehavior=convertToNull";

    DatabaseMySQL() {
        hikariConfig.setPoolName("Alix MySQL Pool");
        //Is this fine?
        hikariConfig.setDriverClassName("mariadb.jdbc.Driver");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setUsername(DatabaseConfig.NAME);
        hikariConfig.setPassword(DatabaseConfig.PASSWORD);
        hikariConfig.setJdbcUrl(JDBC_URL
                .replace("%host%", DatabaseConfig.HOST)
                .replace("%port%", String.valueOf(DatabaseConfig.getPort(this.getType())))
                .replace("%database%", DatabaseConfig.NAME));
        hikariConfig.setMaxLifetime(DatabaseConfig.MAX_LIFETIME);
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MY_SQL;
    }
}