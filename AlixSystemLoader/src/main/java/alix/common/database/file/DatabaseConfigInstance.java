package alix.common.database.file;

import alix.common.AlixCommonMain;

import java.io.File;

import static alix.common.database.file.DatabaseConfig.config;

public final class DatabaseConfigInstance {

    public final String USERNAME = getString("username", "root");
    public final String PASSWORD = getString("password", "");
    public final String HOST = getString("host", "127.0.0.1");
    private final String SQLITE_PATH = getString("sqlite-path", "AuthMe/authme.db");
    public final File SQLITE_FILE = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolder().getParentFile() + File.separator + SQLITE_PATH.replace('/', File.separatorChar));
    public final int MAX_LIFETIME = getInt("max-lifetime", 60) * 1000;
    private final boolean migrate;

    public DatabaseConfigInstance(boolean migrate) {
        this.migrate = migrate;
        //WrapperConfigServerShowDialog

        if (migrate && !SQLITE_FILE.exists()) {
            AlixCommonMain.logWarning("Migrate SQLITE_FILE of the path \"" + SQLITE_FILE + "\" does not exist!");
        }
    }

    public boolean isMigrate() {
        return migrate;
    }

    public String TABLE_NAME() {
        return getString("table-name", "");
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(this.correctPath(path));
    }

    public String getString(String path, String def) {
        return config.getString(this.correctPath(path), def);
    }

    public int getInt(String path, int def) {
        return config.getInt(this.correctPath(path), def);
    }

    private String correctPath(String path) {
        return (this.migrate ? "migrate-" : "") + path;
    }
}