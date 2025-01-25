package alix.common.database.migrate;

import java.sql.ResultSet;

public interface MigrateSQLProvider {

    void migrateEntry(ResultSet rs) throws Exception;

}