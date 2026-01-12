package alix.common.database.connect;

import alix.common.database.file.DatabaseConfig;
import alix.common.database.file.DatabaseConfigInstance;

import java.util.function.Function;

public enum DatabaseType {

    MYSQL,
    SQLITE,
    POSTGRES;

    public DatabaseConnector getConnector(boolean migrate) {
        Function<DatabaseConfigInstance, DatabaseConnector> func = (switch (this) {
            case SQLITE -> DatabaseSQLite::new;
            case MYSQL, POSTGRES -> DatabaseMySQL::new;
        });
        return func.apply(DatabaseConfig.config(migrate));
    }
}