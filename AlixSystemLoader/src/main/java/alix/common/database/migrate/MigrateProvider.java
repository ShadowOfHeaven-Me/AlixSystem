package alix.common.database.migrate;

import alix.common.AlixCommonMain;

import java.net.InetAddress;
import java.sql.ResultSet;

public interface MigrateProvider {

    void migrateEntry(ResultSet rs) throws Exception;

    default void error(String error) {
        AlixCommonMain.logError(error);
    }

    InetAddress UNKNOWN_ADDRESS = InetAddress.getLoopbackAddress();

}