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
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.keys.secret.MapSecretKey;

import java.net.InetAddress;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static alix.common.database.QueryConstants.*;
import static ua.nanit.limbo.util.UUIDUtil.getOfflineModeUuid;

final class DatabaseUpdaterImpl implements DatabaseUpdater {

    private static final LocationListProvider HOMES_PROVIDER = LocationListProvider.IMPL;
    private final DatabaseConnector database;

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
            }
        });
    }

    @Override
    public void saveUserToken(Identity identity, String token) {
        UUID tokenUuid = getOfflineModeUuid(identity.identity());
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_TOKEN_SQL(this.getType()))) {
                setUuid(ps, 1, tokenUuid);
                ps.setString(2, token);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public PersistentUserData loadUser(String name) {
        AtomicReference<PersistentUserData> result = new AtomicReference<>();

        this.query(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_USER_SQL)) {
                ps.setString(1, name);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        result.set(readUser(rs));
                }
            }
        });

        return result.get();
    }

    private PersistentUserData readUser(ResultSet rs) throws SQLException {
        int i = 1;

        String name = rs.getString(i++);
        UUID uuid = readUuid(rs, i++);
        long createdAt = rs.getLong(i++);
        Long lastSuccessfulLogin = getNullableLong(rs, i++);
        String ipStr = rs.getString(i++);
        long mutedUntil = rs.getLong(i++);

        LoginType loginType = LoginType.valueOf(rs.getString(i++));

        String extraLoginTypeStr = rs.getString(i++);
        LoginType extraLoginType = extraLoginTypeStr == null ? null : LoginType.valueOf(extraLoginTypeStr);

        Boolean ipAutoLogin = getNullableBoolean(rs, i++);
        AuthSetting authSettings = AuthSetting.fromString(rs.getString(i++));
        boolean hasProvenAuthAccess = rs.getBoolean(i++);

        String identityStr = rs.getString(i++);
        Identity identity = identityStr == null ? Identity.newIdentity(name) : Identity.fromSaved(name, identityStr);

        String emailSaved = rs.getString(i++);
        String homesSaved = rs.getString(i++);

        int premiumStatus = rs.getInt(i++);
        String premiumUuid = rs.getString(i++);

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
                extraPassword
        );
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
        Object value = rs.getObject(index);
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        return UUID.fromString(value.toString());
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
            return Email.readFromSaved(saved, MapSecretKey.fromName(identity.identity()));
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
        this.queryAsync(connection -> {
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
                ps.setNull(i++, Types.VARCHAR);
            } else {
                ps.setInt(i++, 0);
                ps.setNull(i++, Types.VARCHAR);
            }

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
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setInt(1, 0);
                ps.setNull(2, Types.VARCHAR);
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
        this.queryAsync(connection -> updatePremiumData(connection, name, data));
    }

    @Override
    public void setPassword(String name, Password newPass, Password oldPass) {
        this.queryAsync(connection -> upsertPassword(connection, name, MAIN_PASSWORD_SLOT, newPass));
    }

    @Override
    public void clearPasswordPointer(String name) {
        this.queryAsync(connection -> {
            deletePassword(connection, name, MAIN_PASSWORD_SLOT);
            deletePassword(connection, name, EXTRA_PASSWORD_SLOT);
        });
    }

    @Override
    public void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_LAST_LOGIN_SQL)) {
                ps.setLong(1, lastSuccessfulLogin);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateIpByName(String name, String ip) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_IP_SQL)) {
                ps.setString(1, ip);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updatePasswordByOwner(String ownerName, Password password) {
        this.queryAsync(connection -> upsertPassword(connection, ownerName, MAIN_PASSWORD_SLOT, password));
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
        this.queryAsync(connection -> {
            try (var stmt = connection.prepareStatement(query)) {
                stmt.execute();
            }
        });
    }

    private static final class AutoErrorReport implements ThrowableConsumer<Connection, Exception> {

        final ThrowableConsumer<Connection, Exception> delegate;

        private AutoErrorReport(ThrowableConsumer<Connection, Exception> delegate) {
            this.delegate = delegate;
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
                        var errAt = stackTrace[i - 1];//>= 0
                        int line = errAt.getLineNumber();
                        AlixCommonMain.logError("Error at line=" + line + ", type=" + DatabaseUpdater.INSTANCE.getType() + ", in=" + errAt.getMethodName());
                        break;
                    }
                }
                AlixCommonUtils.logException(t);
            }
        }
    }

    void queryAsync(ThrowableConsumer<Connection, Exception> func) {
        this.async(() -> this.query(func));
    }

    void query(ThrowableConsumer<Connection, Exception> func) {
        this.database.query(new AutoErrorReport(func));
    }

    void async(Runnable r) {
        AlixScheduler.asyncBlocking(r);
    }
}