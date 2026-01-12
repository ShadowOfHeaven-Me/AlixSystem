package alix.common.database.connect;

import alix.common.database.file.DatabaseConfig;
import alix.common.database.file.DatabaseConfigInstance;
import lombok.SneakyThrows;
import org.mariadb.jdbc.Driver;

import java.lang.invoke.MethodHandles;

final class DatabaseMySQL extends AbstractDatabaseConnector {

    //Source code: https://github.com/kyngs/LibreLogin/blob/5cae5bd01fa37e45ee1d529bfa5c8eb1dcdc5c58/Plugin/src/main/java/xyz/kyngs/librelogin/common/database/connector/AuthenticMySQLDatabaseConnector.java

    private static final String JDBC_URL = "jdbc:mariadb://%host%:%port%/%database%?autoReconnect=true&zeroDateTimeBehavior=convertToNull";

    @SneakyThrows
    DatabaseMySQL(DatabaseConfigInstance config) {
        hikariConfig.setPoolName("Alix MySQL Pool");
        //Is this fine?
        //AlixCommonMain.logError("shii=" + Thread.currentThread().getContextClassLoader() + ", " + BukkitAlixMain.class.getClassLoader());
        //Thread.currentThread().setContextClassLoader(AlixCommonMain.MAIN_CLASS_INSTANCE.getClass().getClassLoader());
        MethodHandles.lookup().ensureInitialized(Driver.class);
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setUsername(config.USERNAME);
        hikariConfig.setPassword(config.PASSWORD);
        hikariConfig.setJdbcUrl(JDBC_URL
                .replace("%host%", config.HOST)
                .replace("%port%", String.valueOf(DatabaseConfig.getPort(this.getType())))
                .replace("%database%", config.isMigrate() ? config.TABLE_NAME() : "alix"));
        hikariConfig.setMaxLifetime(config.MAX_LIFETIME);
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MYSQL;
    }
}