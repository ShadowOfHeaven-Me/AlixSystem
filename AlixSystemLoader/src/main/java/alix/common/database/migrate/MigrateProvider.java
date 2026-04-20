package alix.common.database.migrate;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;

import java.net.InetAddress;
import java.sql.ResultSet;

public interface MigrateProvider {

    void migrateEntry(ResultSet rs) throws Exception;

    default void error(String error) {
        AlixCommonMain.logError(error);
    }

    InetAddress UNKNOWN_ADDRESS = PersistentUserData.UNKNOWN_IP;

}