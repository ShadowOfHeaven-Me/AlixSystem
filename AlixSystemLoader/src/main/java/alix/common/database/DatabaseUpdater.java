package alix.common.database;

import alix.common.AlixCommonMain;
import alix.common.data.Identity;
import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseType;
import alix.common.database.file.DatabaseConfig;
import alix.common.utils.config.ConfigParams;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.UUID;

public interface DatabaseUpdater {

    default void testDatabase() {
        String player = "sex";
        //this.insertUser(player, UUID.nameUUIDFromBytes(player.getBytes(StandardCharsets.UTF_8)), System.currentTimeMillis(), Password.createRandom());
        PersistentUserData.createDefault(player, InetAddress.getLoopbackAddress(), Password.createRandom());
        this.clearPasswordPointer(player);
        this.updateLastSuccessfulLoginByName(player, 96_240L);
        this.updateIpByName(player, "127.0.0.1");
        this.updatePasswordByOwner(player, Password.fromUnhashed("zpedałami"));
        this.setPremiumData(player, PremiumData.createNew(UUID.randomUUID()));
    }

    void connect();

    void saveData(PersistentUserData data);

    void createTablesSync();

    void clearPasswordPointer(String name);

    void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin);

    void updateIpByName(String name, String ip);

    void updatePasswordByOwner(String ownerName, Password password);

    void setPremiumData(String name, PremiumData data);

    void setPassword(String name, Password newPass, Password oldPass);

    void saveUserToken(Identity identity, String token);

    PersistentUserData loadUser(String name);

    DatabaseType getType();

    DatabaseUpdater NOOP = new NoOPDatabaseImpl();
    DatabaseUpdater INSTANCE = define0();

    private static DatabaseUpdater define0() {
        var config = DatabaseConfig.EXTERNAL;
        boolean useThis = config.getBoolean("external-sync");
        if (!useThis) return NOOP;

        var typeStr = config.getString("database-type", "MySQL");
        DatabaseType type;
        try {
            type = DatabaseType.valueOf(typeStr.toUpperCase());
        } catch (Exception e) {
            AlixCommonMain.logWarning("Invalid database-type in database.yml, available: " + Arrays.toString(DatabaseType.values()) + ". Using default 'MYSQL'.");
            type = DatabaseType.MYSQL;
        }
        var db = new DatabaseUpdaterImpl(type.getConnector(false));

        try {
            db.connect();
            return db;
        } catch (Throwable ex) {
            AlixCommonMain.logWarning("Could not connect to database: '" + ex.getMessage()
                                      + (!ConfigParams.isDebugEnabled ? "' Enable 'debug' for full stacktrace" : "") + ". Using default NO-OP");
            if (ConfigParams.isDebugEnabled)
                ex.printStackTrace();
            return NOOP;
        }
    }
}
