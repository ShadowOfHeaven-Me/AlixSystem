package alix.common.database.connect;

import alix.common.database.file.DatabaseConfig;
import alix.common.database.file.DatabaseConfigInstance;

//Src: https://github.com/kyngs/LibreLogin/blob/5cae5bd01fa37e45ee1d529bfa5c8eb1dcdc5c58/Plugin/src/main/java/xyz/kyngs/librelogin/common/database/connector/AuthenticMySQLDatabaseConnector.java

final class DatabasePostgreSQL extends AbstractDatabaseConnector {

    DatabasePostgreSQL(DatabaseConfigInstance config) {
        hikariConfig.setPoolName("Alix PostgreSQL Pool");
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("ssl", "false");
        hikariConfig.addDataSourceProperty("sslmode", "disable");

        hikariConfig.setUsername(config.USERNAME);
        hikariConfig.setPassword(config.PASSWORD);
        hikariConfig.setJdbcUrl("jdbc:postgresql://" + config.HOST + ":" + DatabaseConfig.getPort(this.getType()) + "/" + config.USER + "?sslmode=disable&autoReconnect=true&zeroDateTimeBehavior=convertToNull&ssl=false");
        hikariConfig.setMaxLifetime(config.MAX_LIFETIME);
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.POSTGRESQL;
    }
}
