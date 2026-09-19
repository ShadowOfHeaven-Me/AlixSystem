package alix.common.database;

import alix.common.AlixCommonMain;
import alix.common.data.AuthSetting;
import alix.common.data.Identity;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseType;
import alix.common.database.file.DatabaseConfig;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.other.keys.secret.MapSecretKey;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface DatabaseUpdater {

    default void testDatabase() {
        String player = "sex";
        //this.insertUser(player, UUID.nameUUIDFromBytes(player.getBytes(StandardCharsets.UTF_8)), System.currentTimeMillis(), Password.createRandom());
        PersistentUserData.createDefault(player, InetAddress.getLoopbackAddress(), Password.createRandom());
        this.clearPasswordPointers(player);
        this.updateLastSuccessfulLoginByName(player, 96_240L);
        this.updateIpByName(player, "127.0.0.1");
        this.updatePasswordByOwner(player, Password.fromUnhashed("zpedałami"));
        this.setPremiumData(player, PremiumData.createNew(UUID.randomUUID()));
    }

    default boolean isImpl() {
        return this instanceof DatabaseUpdaterImpl;
    }

    CompletableFuture<Void> loadAllUsers(Map<String, PersistentUserData> map);

    CompletableFuture<Void> loadAllTokens(Map<MapSecretKey, String> map);

    void connect();

    void saveData(PersistentUserData data);

    void createTablesSync();

    void clearPasswordPointers(String name);

    void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin);

    void updateIpByName(String name, String ip);

    void updateFingerprintByName(String name, int fingerprint);

    void updatePasswordByOwner(String ownerName, Password password);

    void setPremiumData(String name, PremiumData data);

    void setPassword(String name, Password newPass, boolean isMain);

    void saveUserToken(Identity identity, String token);

    //Atomic token+email commit for a 2FA reset - see DatabaseUpdaterImpl's implementation for why a plain
    //UPSERT_TOKEN_SQL upsert followed by a separate updateEmailByName() call isn't good enough here.
    //savableEmail may be null (no email to update).
    void commitTokenAndEmail(Identity identity, String token, String name, String savableEmail);

    void saveRecoveryCodes(Identity identity, String joinedCodes);

    void loadRecoveryCodes(Identity identity, Consumer<String> consumer);

    //Atomic read-check-write, unlike a caller doing loadRecoveryCodes() then saveRecoveryCodes() itself -
    //see DatabaseUpdaterImpl's implementation for why that split would race a concurrent attempt for the
    //same player.
    void tryConsumeRecoveryCode(Identity identity, String typedCode, Consumer<Boolean> callback);

    void loadUser(String name, Consumer<PersistentUserData> consumer);

    void updateAuthSettingsByName(String name, AuthSetting authSettings);

    void updateHasProvenAuthAccessByName(String name, boolean hasProvenAuthAccess);

    void updateIpAutoLoginByName(String name, Boolean ipAutoLogin);

    void updateLoginTypeByName(String name, LoginType loginType);

    void updateExtraLoginTypeByName(String name, LoginType extraLoginType);

    //identity is only used to key this write onto the same per-player execution chain commitTokenAndEmail()
    //uses - see DatabaseUpdaterImpl's implementation for why.
    void updateEmailByName(Identity identity, String name, String email);

    void removeByName(String name);

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
