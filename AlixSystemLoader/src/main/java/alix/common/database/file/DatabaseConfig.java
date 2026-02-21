package alix.common.database.file;

import alix.common.database.connect.DatabaseType;
import alix.common.utils.config.alix.AlixYamlConfig;
import alix.common.utils.file.AlixFileManager;

public final class DatabaseConfig {

    //Source code: https://github.com/kyngs/LibreLogin/blob/5cae5bd01fa37e45ee1d529bfa5c8eb1dcdc5c58/Plugin/src/main/java/xyz/kyngs/librelogin/common/database/connector/AuthenticMySQLDatabaseConnector.java

    public static final AlixYamlConfig config;

    static {
        var file = AlixFileManager.getOrCreatePluginFile("database.yml", AlixFileManager.FileType.CONFIG);
        config = new AlixYamlConfig(file);
    }

    public static final DatabaseConfigInstance MIGRATE = new DatabaseConfigInstance(true);
    public static final DatabaseConfigInstance EXTERNAL = new DatabaseConfigInstance(false);

    public static DatabaseConfigInstance config(boolean migrate) {
        return migrate ? MIGRATE : EXTERNAL;
    }

    public static int getPort(DatabaseType type) {
        int def = switch (type) {
            case MYSQL -> 3306;
            case POSTGRESQL -> 5432;
            case SQLITE -> -1;
        };

        return config.getInt("port", def);
    }
}