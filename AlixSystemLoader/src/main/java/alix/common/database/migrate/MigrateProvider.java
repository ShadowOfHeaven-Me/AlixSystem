package alix.common.database.migrate;

import alix.common.AlixCommonMain;

import java.sql.ResultSet;

public interface MigrateProvider {

    void migrateEntry(ResultSet rs) throws Exception;

    default void error(String error) {
        AlixCommonMain.logError(error);
    }
}