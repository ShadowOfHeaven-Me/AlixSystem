package alix.common.database;

import alix.common.AlixCommonMain;
import alix.common.data.premium.PremiumData;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseType;
import alix.common.database.file.DatabaseConfig;
import alix.common.utils.config.ConfigParams;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public interface DatabaseUpdater {

    default void testDatabase() {
        String player = "sex";
        this.insertUser(player, UUID.nameUUIDFromBytes(player.getBytes(StandardCharsets.UTF_8)), System.currentTimeMillis(), Password.createRandom());
        this.clearPasswordPointer(player);
        this.updateLastSuccessfulLoginByName(player, 96_240L);
        this.updateIpByName(player, "127.0.0.1");
        this.updatePasswordByOwner(player, Password.fromUnhashed("zpedałami"));
        this.setPremiumData(player, PremiumData.createNew(UUID.randomUUID()));
    }

    void connect();

    void createTablesSync();

    void insertUser(String name, UUID uuid, long createdAt, Password password);

    void addPasswordAndLink(String ownerName, Password password);

    void clearPasswordPointer(String name);

    void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin);

    void updateIpByName(String name, String ip);

    void updatePasswordByOwner(String ownerName, Password password);

    void setPremiumData(String name, PremiumData data);

    void setPassword(String name, Password newPass, Password oldPass);

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

    /**
     * Fetch a user with its linked password (if any) by user name.
     * The result is delivered to onResult (Optional.empty() if not found).
     */
    /*public void findUserWithPasswordByName(String name, Consumer<Optional<UserWithPassword>> onResult) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_USER_WITH_PASSWORD_BY_NAME_SQL)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        onResult.accept(Optional.empty());
                        return;
                    }
                    UserWithPassword dto = new UserWithPassword();
                    dto.name = rs.getString("name");
                    dto.uuid = (UUID) rs.getObject("uuid");
                    dto.createdAt = rs.getLong("created_at");
                    long lastLogin = rs.getLong("last_successful_login");
                    dto.lastSuccessfulLogin = rs.wasNull() ? null : lastLogin;
                    dto.ip = rs.getString("ip");
                    long pwdPointer = rs.getLong("password_id");
                    dto.passwordId = rs.wasNull() ? null : pwdPointer;

                    long pwdId = rs.getLong("id");
                    if (!rs.wasNull()) {
                        dto.password = new PasswordRow();
                        dto.password.id = pwdId;
                        dto.password.hashedPassword = rs.getString("hashed_password");
                        dto.password.hashId = (byte) rs.getShort("hash_id");
                        dto.password.salt = rs.getString("salt");
                        dto.password.matcherId = (byte) rs.getShort("matcher_id");
                    }
                    onResult.accept(Optional.of(dto));
                }
            }
        });
    }*/
}
