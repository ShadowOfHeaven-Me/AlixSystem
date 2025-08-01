package alix.common.database.file;

import alix.common.AlixCommonMain;
import alix.common.database.connect.DatabaseType;
import alix.common.utils.config.alix.AlixYamlConfig;
import alix.common.utils.file.AlixFileManager;

import java.io.File;

public final class DatabaseConfig {

    //Source code: https://github.com/kyngs/LibreLogin/blob/5cae5bd01fa37e45ee1d529bfa5c8eb1dcdc5c58/Plugin/src/main/java/xyz/kyngs/librelogin/common/database/connector/AuthenticMySQLDatabaseConnector.java

    private static final AlixYamlConfig config;

    static {
        var file = AlixFileManager.getOrCreatePluginFile("database.yml", AlixFileManager.FileType.CONFIG);
        config = new AlixYamlConfig(file);
    }

    public static final String TABLE_NAME = config.getString("table-name", "");
    public static final String USERNAME = config.getString("username", "root");
    public static final String PASSWORD = config.getString("password", "");
    public static final String HOST = config.getString("host", "127.0.0.1");
    private static final String SQLITE_PATH = config.getString("sqlite-path", "AuthMe/authme.db");
    public static final File SQLITE_FILE = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolder().getParentFile() + File.separator + SQLITE_PATH.replace('/', File.separatorChar));
    public static final int MAX_LIFETIME = config.getInt("max-lifetime", 60) * 1000;

    static {
        if (!SQLITE_FILE.exists()) {
            AlixCommonMain.logWarning("SQLITE_FILE of the path \"" + SQLITE_FILE + "\" does not exist!");
        }
    }

    public static int getPort(DatabaseType type) {
        int def = switch (type) {
            case MY_SQL -> 3306;
            case POSTGRES -> 5432;
            case SQLITE -> -1;
        };

        return config.getInt("port", def);
    }
}