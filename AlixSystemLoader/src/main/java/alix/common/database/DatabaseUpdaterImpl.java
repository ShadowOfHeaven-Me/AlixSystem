package alix.common.database;

import alix.common.AlixCommonMain;
import alix.common.antibot.ip.IPUtils;
import alix.common.data.*;
import alix.common.data.loc.AlixLocationList;
import alix.common.data.loc.provider.LocationListProvider;
import alix.common.data.premium.PremiumData;
import alix.common.data.security.email.Email;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseConnector;
import alix.common.database.connect.DatabaseType;
import alix.common.login.auth.RecoveryCodes;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.keys.secret.MapSecretKey;

import java.net.InetAddress;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static alix.common.database.QueryConstants.*;

final class DatabaseUpdaterImpl implements DatabaseUpdater {

    private static final LocationListProvider HOMES_PROVIDER = LocationListProvider.IMPL;
    private final DatabaseConnector database;

    // Stores active execution chains per player key to guarantee FIFO ordering per player
    private final Map<String, CompletableFuture<Void>> playerExecutionChains = new ConcurrentHashMap<>(); //AlixCache.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).<String, CompletableFuture<Void>>build().asMap();

    DatabaseUpdaterImpl(DatabaseConnector database) {
        this.database = database;
    }

    @Override
    public void connect() {
        this.database.connect();
    }

    @Override
    public void createTablesSync() {
        this.query(connection -> {
            try (var st = connection.createStatement()) {
                st.execute(CREATE_USERS_SQL(this.getType()));
                st.execute(CREATE_PASSWORDS_SQL(this.getType()));
                st.execute(CREATE_TOKENS_SQL(this.getType()));

                //a fresh table already has the column via CREATE_USERS_SQL above - this only matters for a
                //table that pre-dates the 'fingerprint' column being introduced
                try {
                    st.execute(ADD_FINGERPRINT_COLUMN_SQL(this.getType()));
                } catch (SQLException ignored) {
                    //older engine without "ADD COLUMN IF NOT EXISTS" support and the column already exists
                }

                //same reasoning as fingerprint above, for the 'recovery_codes' column on alix_user_tokens
                try {
                    st.execute(ADD_RECOVERY_CODES_COLUMN_SQL(this.getType()));
                } catch (SQLException ignored) {
                    //older engine without "ADD COLUMN IF NOT EXISTS" support and the column already exists
                }
            }
        });
    }

    /**
     * Enqueues an asynchronous query sequentially for a specific player key.
     *
     * @param playerKey Player name or identifier
     * @param func      Database query callback
     */
    void queryAsync(String playerKey, ThrowableConsumer<Connection, Exception> func) {
        if (playerKey == null) {
            // Unbound query fallback
            this.async(() -> this.query(func));
            return;
        }

        playerExecutionChains.compute(playerKey, (k, currentChain) -> {
            CompletableFuture<Void> nextTask;

            if (currentChain == null) {
                // First query
                nextTask = CompletableFuture.runAsync(() -> this.query(func), AlixScheduler::asyncBlocking);
            } else {
                // Chain execution
                nextTask = currentChain.handleAsync((res, ex) -> {
                    this.query(func);
                    return null;
                }, AlixScheduler::asyncBlocking);
            }

            // clean-up
            nextTask.whenComplete((res, ex) -> playerExecutionChains.remove(k, nextTask));
            return nextTask;
        });
    }

    @Override
    public void saveUserToken(Identity identity, String token) {
        UUID tokenUuid = identity.tokenKey().key();
        this.queryAsync(identity.identity(), connection -> {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_TOKEN_SQL(this.getType()))) {
                setUuid(ps, 1, tokenUuid);
                ps.setString(2, token);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void overwriteUserToken(Identity identity, String token) {
        UUID tokenUuid = identity.tokenKey().key();
        this.queryAsync(identity.identity(), connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPSERT_TOKEN_SQL(this.getType()))) {
                setUuid(ps, 1, tokenUuid);
                ps.setString(2, token);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void saveRecoveryCodes(Identity identity, String joinedCodes) {
        UUID tokenUuid = identity.tokenKey().key();
        this.queryAsync(identity.identity(), connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_RECOVERY_CODES_SQL)) {
                ps.setString(1, joinedCodes);
                setUuid(ps, 2, tokenUuid);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void loadRecoveryCodes(Identity identity, Consumer<String> consumer) {
        UUID tokenUuid = identity.tokenKey().key();
        //queryAsync (not query!) - this is called directly from GUI click handlers/the main login thread,
        //and query() blocks the calling thread on a real DB round-trip; queryAsync offloads it to a
        //background thread, same as every write method above. Also chains onto this player's own
        //execution queue (keyed by identity.identity(), same key saveRecoveryCodes()/tryConsumeRecoveryCode()
        //below use), so a read here can never interleave with a concurrent write for the SAME player.
        this.queryAsync(identity.identity(), connection -> {
            try (PreparedStatement ps = connection.prepareStatement(LOAD_RECOVERY_CODES_SQL)) {
                setUuid(ps, 1, tokenUuid);

                try (ResultSet rs = ps.executeQuery()) {
                    consumer.accept(rs.next() ? rs.getString(1) : null);
                }
            }
        });
    }

    //Reads, checks and (on a match) rewrites the recovery-codes row all within ONE queryAsync task (one
    //connection, one entry in this player's execution chain) - unlike doing loadRecoveryCodes() then a
    //separate saveRecoveryCodes() call from the caller, this can't be interleaved by a second concurrent
    //attempt for the same player (e.g. a laggy client double-sending "/recoverycode <code>"): the second
    //attempt is queued behind this ENTIRE read-check-write, not just behind the read half of it, so it
    //always sees this attempt's result (code removed if consumed) rather than racing it.
    @Override
    public void tryConsumeRecoveryCode(Identity identity, String typedCode, Consumer<Boolean> callback) {
        UUID tokenUuid = identity.tokenKey().key();
        this.queryAsync(identity.identity(), connection -> {
            String joined;
            try (PreparedStatement ps = connection.prepareStatement(LOAD_RECOVERY_CODES_SQL)) {
                setUuid(ps, 1, tokenUuid);
                try (ResultSet rs = ps.executeQuery()) {
                    joined = rs.next() ? rs.getString(1) : null;
                }
            }

            String[] codes = RecoveryCodes.split(joined);
            int matchIndex = -1;

            for (int i = 0; i < codes.length; i++) {
                if (RecoveryCodes.matches(codes[i], typedCode)) {
                    matchIndex = i;
                    break;
                }
            }

            if (matchIndex < 0) {
                callback.accept(false);
                return;
            }

            String[] remaining = new String[codes.length - 1];
            System.arraycopy(codes, 0, remaining, 0, matchIndex);
            System.arraycopy(codes, matchIndex + 1, remaining, matchIndex, codes.length - matchIndex - 1);

            try (PreparedStatement ps = connection.prepareStatement(UPDATE_RECOVERY_CODES_SQL)) {
                ps.setString(1, RecoveryCodes.join(remaining));
                setUuid(ps, 2, tokenUuid);
                ps.executeUpdate();
            }

            callback.accept(true);
        });
    }

    @Override
    public CompletableFuture<Void> loadAllUsers(Map<String, PersistentUserData> map) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        // Uses a forward-only fetch strategy for fast bulk streaming
        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(LOAD_ALL_USERS);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    var data = readData(rs);
                    map.put(data.getName(), data);
                }
                future.complete(null);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> loadAllTokens(Map<MapSecretKey, String> map) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(LOAD_ALL_TOKENS);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    var uuid = readUuid(rs, 1);
                    var token = rs.getString(2);
                    map.put(MapSecretKey.uuidKey(uuid), token);
                }
                future.complete(null);
            }
        });
        return future;
    }

    @Override
    public void loadUser(String name, Consumer<PersistentUserData> consumer) {
        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_USER_SQL)) {
                ps.setString(1, name);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        consumer.accept(readData(rs));
                }
            }
        });
    }

    private PersistentUserData readData(ResultSet rs) throws SQLException {
        int i = 1;

        String name = rs.getString(i++);
        UUID uuid = readUuid(rs, i++);
        long createdAt = rs.getLong(i++);
        Long lastSuccessfulLogin = getNullableLong(rs, i++);
        String ipStr = rs.getString(i++);
        long mutedUntil = rs.getLong(i++);

        LoginType loginType = parseEnumSafely(LoginType.class, rs.getString(i++), LoginType.COMMAND);

        String extraLoginTypeStr = rs.getString(i++);
        LoginType extraLoginType = parseEnumSafely(LoginType.class, extraLoginTypeStr, null);

        Boolean ipAutoLogin = getNullableBoolean(rs, i++);
        AuthSetting authSettings = AuthSetting.fromString(rs.getString(i++));
        boolean hasProvenAuthAccess = rs.getBoolean(i++);

        String identityStr = rs.getString(i++);
        Identity identity = identityStr == null ? Identity.newIdentity(name) : Identity.fromSaved(name, identityStr);

        String emailSaved = rs.getString(i++);
        String homesSaved = rs.getString(i++);

        int premiumStatus = rs.getInt(i++);
        String premiumUuid = rs.getString(i++);
        int fingerprint = rs.getInt(i++);

        Password mainPassword = readPassword(rs, i);
        i += 4;

        Password extraPassword = readPassword(rs, i);

        InetAddress ip = ipStr == null ? PersistentUserData.UNKNOWN_IP : IPUtils.fromAddress(ipStr);
        AlixLocationList homes = readHomes(homesSaved);
        PremiumData premiumData = readPremiumData(premiumStatus, premiumUuid);
        Email email = readEmail(emailSaved, identity);

        return PersistentUserData.fromDatabase(
                name,
                uuid,
                createdAt,
                lastSuccessfulLogin == null ? 0L : lastSuccessfulLogin,
                ip,
                mutedUntil,
                loginType,
                extraLoginType,
                ipAutoLogin,
                authSettings,
                hasProvenAuthAccess,
                identity,
                email,
                homes,
                premiumData,
                mainPassword,
                extraPassword,
                fingerprint
        );
    }

    private static <T extends Enum<T>> T parseEnumSafely(Class<T> enumClass, String value, T defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private static Long getNullableLong(ResultSet rs, int index) throws SQLException {
        long value = rs.getLong(index);
        return rs.wasNull() ? null : value;
    }

    private static Boolean getNullableBoolean(ResultSet rs, int index) throws SQLException {
        boolean value = rs.getBoolean(index);
        return rs.wasNull() ? null : value;
    }

    private static UUID readUuid(ResultSet rs, int index) throws SQLException {
        String str = rs.getString(index);
        if (str == null) return null;
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static AlixLocationList readHomes(String saved) {
        if (saved == null || saved.equals(PersistentUserData.NO_VALUE)) {
            return HOMES_PROVIDER.newList();
        }
        return HOMES_PROVIDER.fromSavable(saved);
    }

    private static Email readEmail(String saved, Identity identity) {
        if (saved == null || saved.equals(PersistentUserData.NO_VALUE)) {
            return null;
        }

        try {
            return Email.readFromSaved(saved, identity.getToken());
        } catch (Exception e) {
            AlixCommonMain.logWarning("Failed to load encrypted email for identity=" + identity.identity() + ": " + e.getMessage());
            return null;
        }
    }

    private static PremiumData readPremiumData(int premiumStatus, String premiumUuid) {
        if (premiumStatus == 1 && premiumUuid != null) {
            try {
                return PremiumData.createNew(UUID.fromString(premiumUuid));
            } catch (IllegalArgumentException e) {
                AlixCommonMain.logWarning("Invalid premium UUID found: " + premiumUuid);
                return PremiumData.UNKNOWN;
            }
        }
        return premiumStatus == -1 ? PremiumData.NON_PREMIUM : PremiumData.UNKNOWN;
    }

    private Password readPassword(ResultSet rs, int index) throws SQLException {
        String hashedPassword = rs.getString(index);
        if (hashedPassword == null) {
            return Password.empty();
        }

        byte hashId = (byte) rs.getInt(index + 1);

        String salt = rs.getString(index + 2);
        if (salt == null) {
            salt = "";
        }

        byte matcherId = (byte) rs.getInt(index + 3);

        return Password.fromDatabase(hashedPassword, hashId, salt, matcherId);
    }

    @Override
    public void saveData(PersistentUserData data) {
        this.queryAsync(data.getName(), connection -> {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                upsertUser(connection, data);

                upsertPassword(connection, data.getName(), MAIN_PASSWORD_SLOT, data.getLoginParams().getPassword());

                Password extraPassword = data.getLoginParams().getExtraPassword();
                if (extraPassword == null) {
                    deletePassword(connection, data.getName(), EXTRA_PASSWORD_SLOT);
                } else {
                    upsertPassword(connection, data.getName(), EXTRA_PASSWORD_SLOT, extraPassword);
                }

                connection.commit();
            } catch (Throwable t) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
                throw t;
            } finally {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        });
    }

    private void upsertUser(Connection connection, PersistentUserData data) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPSERT_USER_SQL(this.getType()))) {
            int i = 1;

            ps.setString(i++, data.getName());
            setUuid(ps, i++, data.getUUID());
            ps.setLong(i++, data.createdAt());
            setNullableLong(ps, i++, data.getLastSuccessfulLogin());
            setNullableString(ps, i++, data.getSavedIP() == null ? null : data.getSavedIP().getHostAddress());
            ps.setLong(i++, data.getMutedUntil());

            LoginParams login = data.getLoginParams();

            ps.setString(i++, data.getLoginType().name());

            LoginType extraLoginType = login.getExtraLoginType();
            if (extraLoginType == null) {
                ps.setNull(i++, Types.VARCHAR);
            } else {
                ps.setString(i++, extraLoginType.name());
            }

            Boolean rawIpAutoLogin = login.getRawIpAutoLogin();
            if (rawIpAutoLogin == null) {
                ps.setNull(i++, Types.BOOLEAN);
            } else {
                ps.setBoolean(i++, rawIpAutoLogin);
            }

            ps.setString(i++, login.getAuthSettings().toSavable());
            ps.setBoolean(i++, login.hasProvenAuthAccess());
            ps.setString(i++, data.identity().identity());

            Email email = data.getEmail();
            if (email == null) {
                ps.setNull(i++, Types.LONGVARCHAR);
            } else {
                ps.setString(i++, email.toSavable());
            }

            String homes = data.getHomes() == null ? null : data.getHomes().toSavable();
            setNullableString(ps, i++, homes);

            PremiumData premium = data.getPremiumData();
            if (premium.getStatus().isPremium()) {
                ps.setInt(i++, 1);
                setUuid(ps, i++, premium.premiumUUID());
            } else if (premium.getStatus().isNonPremium()) {
                ps.setInt(i++, -1);
                setUuid(ps, i++, null);
            } else {
                ps.setInt(i++, 0);
                setUuid(ps, i++, null);
            }

            ps.setInt(i++, data.getFingerprint());

            ps.executeUpdate();
        }
    }

    private void updatePremiumData(Connection connection, String name, PremiumData data) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_PREMIUM_SQL)) {
            if (data.getStatus().isPremium()) {
                ps.setInt(1, 1);
                setUuid(ps, 2, data.premiumUUID());
            } else if (data.getStatus().isNonPremium()) {
                ps.setInt(1, -1);
                setUuid(ps, 2, null);
            } else {
                ps.setInt(1, 0);
                setUuid(ps, 2, null);
            }

            ps.setString(3, name);
            ps.executeUpdate();
        }
    }

    private void upsertPassword(Connection connection, String ownerName, int slot, Password password) throws SQLException {
        if (password == null || !password.isSet()) {
            deletePassword(connection, ownerName, slot);
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(UPSERT_PASSWORD_SQL(this.getType()))) {
            ps.setString(1, ownerName);
            ps.setInt(2, slot);
            ps.setString(3, password.getHashedPassword());
            ps.setShort(4, (short) (password.getHashId() & 0xFF));
            ps.setString(5, password.getSalt());
            ps.setShort(6, (short) (password.getMatcherId() & 0xFF));
            ps.executeUpdate();
        }
    }

    private void deletePassword(Connection connection, String ownerName, int slot) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_PASSWORD_SQL)) {
            ps.setString(1, ownerName);
            ps.setInt(2, slot);
            ps.executeUpdate();
        }
    }

    @Override
    public void setPremiumData(String name, PremiumData data) {
        this.queryAsync(name, connection -> updatePremiumData(connection, name, data));
    }

    @Override
    public void setPassword(String name, Password newPass, boolean isMain) {
        this.queryAsync(name, connection -> upsertPassword(connection, name, isMain ? MAIN_PASSWORD_SLOT : EXTRA_PASSWORD_SLOT, newPass));
    }

    @Override
    public void clearPasswordPointers(String name) {
        this.queryAsync(name, connection -> clearPasswordPointers0(name, connection));
    }

    private void clearPasswordPointers0(String name, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(CLEAR_PASSWORD_POINTERS)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin) {
        this.queryAsync(name, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_LAST_LOGIN_SQL)) {
                ps.setLong(1, lastSuccessfulLogin);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateIpByName(String name, String ip) {
        this.queryAsync(name, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_IP_SQL)) {
                ps.setString(1, ip);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateFingerprintByName(String name, int fingerprint) {
        this.queryAsync(name, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_FINGERPRINT_BY_NAME)) {
                ps.setInt(1, fingerprint);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updatePasswordByOwner(String ownerName, Password password) {
        this.queryAsync(ownerName, connection -> upsertPassword(connection, ownerName, MAIN_PASSWORD_SLOT, password));
    }

    @Override
    public void updateAuthSettingsByName(String name, AuthSetting authSettings) {
        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_AUTH_SETTINGS_BY_NAME)) {
                ps.setString(1, authSettings != null ? authSettings.name() : null);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateHasProvenAuthAccessByName(String name, boolean hasProvenAuthAccess) {
        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_HAS_PROVEN_AUTH_ACCESS_BY_NAME)) {
                ps.setBoolean(1, hasProvenAuthAccess);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateIpAutoLoginByName(String name, Boolean ipAutoLogin) {
        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_IP_AUTO_LOGIN_BY_NAME)) {
                ps.setObject(1, ipAutoLogin);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateLoginTypeByName(String name, LoginType loginType) {
        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_LOGIN_TYPE_BY_NAME)) {
                ps.setString(1, loginType != null ? loginType.name() : null);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateExtraLoginTypeByName(String name, LoginType extraLoginType) {
        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_EXTRA_LOGIN_TYPE_BY_NAME)) {
                ps.setString(1, extraLoginType != null ? extraLoginType.name() : null);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateEmailByName(String name, String email) {
        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_EMAIL_BY_NAME)) {
                ps.setString(1, email);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void removeByName(String name) {
        this.query(connection -> {
            this.clearPasswordPointers0(name, connection);
            try (PreparedStatement ps = connection.prepareStatement(REMOVE_USER_BY_NAME)) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
        });
    }

    private void setUuid(PreparedStatement ps, int index, UUID uuid) throws SQLException {
        if (uuid == null) {
            if (this.getType() == DatabaseType.POSTGRESQL) {
                ps.setNull(index, Types.OTHER);
            } else {
                ps.setNull(index, Types.VARCHAR);
            }
        } else {
            if (this.getType() == DatabaseType.POSTGRESQL) {
                ps.setObject(index, uuid);
            } else {
                ps.setString(index, uuid.toString());
            }
        }
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    @Override
    public DatabaseType getType() {
        return this.database.getType();
    }

    void queryAsync(String query) {
        this.async(() -> this.query(connection -> {
            try (var stmt = connection.prepareStatement(query)) {
                stmt.execute();
            }
        }));
    }

    private static final class AutoErrorReport implements ThrowableConsumer<Connection, Exception> {

        final ThrowableConsumer<Connection, Exception> delegate;
        final DatabaseType dbType;

        private AutoErrorReport(ThrowableConsumer<Connection, Exception> delegate, DatabaseType dbType) {
            this.delegate = delegate;
            this.dbType = dbType;
        }

        @Override
        public void apply(Connection obj) {
            try {
                this.delegate.apply(obj);
            } catch (Throwable t) {
                StackTraceElement[] stackTrace = t.getStackTrace();
                for (int i = 1; i < stackTrace.length; i++) {
                    var frame = stackTrace[i];
                    if (frame.getClassName().equals(this.getClass().getName())) {
                        var errAt = stackTrace[i - 1];
                        int line = errAt.getLineNumber();
                        AlixCommonMain.logError("Error at line=" + line + ", type=" + this.dbType + ", in=" + errAt.getMethodName());
                        break;
                    }
                }
                AlixCommonUtils.logException(t);
            }
        }
    }

    void query(ThrowableConsumer<Connection, Exception> func) {
        this.database.query(new AutoErrorReport(func, this.getType()));
    }

    void async(Runnable r) {
        AlixScheduler.asyncBlocking(r);
    }
}