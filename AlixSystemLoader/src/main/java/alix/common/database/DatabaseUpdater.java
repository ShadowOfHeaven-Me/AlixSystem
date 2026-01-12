package alix.common.database;

import alix.common.AlixCommonMain;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseType;
import alix.common.database.file.DatabaseConfig;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.LongConsumer;

public interface DatabaseUpdater {
    void createTablesSync();

    void insertUser(String name, UUID uuid, long createdAt);

    void addPassword(String ownerName, String hashedPassword, byte hashId, String salt, byte matcherId, LongConsumer onSuccess);

    void addPasswordAndLink(String ownerName, Password password);

    void setPasswordPointer(String name, Long passwordId);

    void clearPasswordPointer(String name);

    void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin);

    void updateIpByName(String name, String ip);

    void updatePasswordById(long passwordId, String hashedPassword, byte hashId, String salt, byte matcherId);

    void updatePasswordByOwner(String ownerName, Password password);

    DatabaseUpdater INSTANCE = define0();

    private static DatabaseUpdater define0() {
        var config = DatabaseConfig.EXTERNAL;
        boolean useThis = config.getBoolean("external-sync");
        if (!useThis) return new NoOPDatabaseImpl();

        var typeStr = config.getString("database-type", "MySQL");
        DatabaseType type;
        try {
            type = DatabaseType.valueOf(typeStr.toUpperCase());
        } catch (Exception e) {
            AlixCommonMain.logWarning("Invalid database-type in database.yml, available: " + Arrays.toString(DatabaseType.values()) + ". Using default 'MYSQL'.");
            type = DatabaseType.MYSQL;
        }
        return new DatabaseUpdaterImpl(type.getConnector(false));
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
