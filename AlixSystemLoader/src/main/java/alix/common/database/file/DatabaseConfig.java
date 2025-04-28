package alix.common.database.file;

import alix.common.database.connect.DatabaseType;
import alix.common.utils.config.alix.AlixYamlConfig;
import alix.common.utils.file.AlixFileManager;

public final class DatabaseConfig {

    //Source code: https://github.com/kyngs/LibreLogin/blob/5cae5bd01fa37e45ee1d529bfa5c8eb1dcdc5c58/Plugin/src/main/java/xyz/kyngs/librelogin/common/database/connector/AuthenticMySQLDatabaseConnector.java

    private static final AlixYamlConfig config;

    static {
        var file = AlixFileManager.getOrCreatePluginFile("database.yml", AlixFileManager.FileType.CONFIG);
        config = new AlixYamlConfig(file);
    }

    public static final String NAME = config.getString("name", "alix");
    public static final String USERNAME = config.getString("username", "root");
    public static final String PASSWORD = config.getString("password", "");
    public static final String HOST = config.getString("host", "localhost");
    public static final int MAX_LIFETIME = config.getInt("max-lifetime", 600000);

    public static int getPort(DatabaseType type) {
        int def = switch (type) {
            case MY_SQL -> 3306;
            case POSTGRES -> 5432;
        };

        return config.getInt("port", def);
    }
}