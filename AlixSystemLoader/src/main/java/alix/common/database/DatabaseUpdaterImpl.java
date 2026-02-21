package alix.common.database;

import alix.common.AlixCommonMain;
import alix.common.data.premium.PremiumData;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseConnector;
import alix.common.database.connect.DatabaseType;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.throwable.AlixException;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.UUID;

import static alix.common.database.QueryConstants.*;

final class DatabaseUpdaterImpl implements DatabaseUpdater {

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
        this.query(conn -> {
            try (var st = conn.createStatement()) {
                st.execute(CREATE_USERS_SQL());
                //AlixCommonMain.logError(CREATE_PASSWORDS_SQL());
                st.execute(CREATE_PASSWORDS_SQL());
                st.execute(CREATE_PREMIUM_DATA_SQL());
                // Try to add FK once. Some DBs may throw if already added; ignore that case.
                try {
                    DatabaseMetaData md = conn.getMetaData();
                    boolean fkExists = false;
// NOTE: getImportedKeys parameters are (catalog, schema, table)
                    try (ResultSet rs = md.getImportedKeys(conn.getCatalog(), null, "alix_users")) {
                        while (rs.next()) {
                            String fkColumn = rs.getString("FKCOLUMN_NAME");   // column in alix_users
                            String pkTable  = rs.getString("PKTABLE_NAME");    // referenced table
                            // You can also read FK_NAME if you want: rs.getString("FK_NAME")
                            if ("password_id".equalsIgnoreCase(fkColumn)
                                && "alix_passwords".equalsIgnoreCase(pkTable)) {
                                fkExists = true;
                                break;
                            }
                        }
                    }
                    if (!fkExists) {
                        st.execute(ADD_FK_USERS_PASSWORD);
                    }
                } catch (SQLException e) {
                    // If constraint already exists, ignore. Otherwise rethrow.
                    // Postgres duplicate_object is SQLState 42710; but we just ignore any
                    // "already exists" style errors because the tables and constraint are idempotent.
                    String sqlState = e.getSQLState();
                    if (sqlState == null || !(sqlState.equals("42710") || sqlState.equals("42P07"))) {
                        // rethrow if it's some other error
                        throw e;
                    }
                }
            }
        });
    }

    void addPremiumData(String name, String premiumUuid) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_PREMIUM_DATA_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, premiumUuid);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        this.setPremiumPointerToId0(connection, name, id);
                    } else {
                        throw new AlixException("Inserting premium_data did not return generated id");
                    }
                }
            }
        });
    }

    @SneakyThrows
    void setPremiumPointerToId0(Connection connection, String name, long premiumDataId) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_PREMIUM_POINTER_BY_NAME_SQL)) {
            ps.setLong(1, premiumDataId);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    @Override
    public void setPremiumData(String name, PremiumData data) {
        if (data.getStatus().isPremium()) {
            this.addPremiumData(name, data.premiumUUID().toString());
            return;
        }
        this.queryAsync(connection -> {
            this.setPremiumPointerToId0(connection, name, data.getStatus().isNonPremium() ? -1 : 0);
        });
    }

    void setPassword0(Connection connection, String name, Password newPass, Password oldPass) throws SQLException {
        //password reset
        if (!newPass.isSet()) {
            this.clearPasswordPointer0(connection, name);
            return;
        }

        //first-time/password reset register
        if (!oldPass.isSet()) {
            this.addPasswordAndLink0(connection, name, newPass);
            return;
        }

        //just a password change
        this.updatePasswordByOwner0(connection, name, newPass);
    }

    @Override
    public void setPassword(String name, Password newPass, Password oldPass) {
        this.queryAsync(connection -> {
            this.setPassword0(connection, name, newPass, oldPass);
        });
    }

    @Override
    public void insertUser(String name, UUID uuid, long createdAt, Password password) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_USER_SQL())) {
                ps.setString(1, name);
                ps.setObject(2, uuid); // driver may accept UUID; otherwise use uuid.toString()
                ps.setLong(3, createdAt);
                ps.executeUpdate();
            }
            this.setPassword0(connection, name, password,
                    Password.empty()//it's fine to call addPasswordAndLink0 even if a linked password exists
            );
        });
    }

    private void addPasswordAndLink0(Connection connection, String ownerName, Password password) throws SQLException {
        boolean origAuto = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            long pwdId;
            // insert password
            try (PreparedStatement ps = connection.prepareStatement(INSERT_PASSWORD_SQL(), Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, ownerName);
                ps.setString(2, password.getHashedPassword());
                ps.setShort(3, (short) (password.getHashId() & 0xFF));
                ps.setString(4, password.getSalt());
                ps.setShort(5, (short) (password.getMatcherId() & 0xFF));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pwdId = rs.getLong(1);
                    } else {
                        throw new AlixException("Insert password did not return generated id");
                    }
                }
            }
            // update user pointer
            try (PreparedStatement ps2 = connection.prepareStatement(UPDATE_USERS_PASSWORD_ID_BY_NAME_SQL)) {
                ps2.setLong(1, pwdId);
                ps2.setString(2, ownerName);
                int updated = ps2.executeUpdate();
                if (updated == 0) {
                    connection.rollback();
                    throw new AlixException("User not found for ownerName: " + ownerName);
                }
            }
            connection.commit();
            //if (onSuccess != null) onSuccess.accept(pwdId);
        } catch (AlixException e) {
            try {
                connection.rollback();
            } catch (Exception ignore) {
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(origAuto);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Atomically insert a password and link it to the user (update users.password_id).
     * onSuccess receives the generated password id.
     */
    @Override
    public void addPasswordAndLink(String ownerName, Password password) {
        this.queryAsync(connection -> {
            this.addPasswordAndLink0(connection, ownerName, password);
        });
    }

    /**
     * Explicit helper to set users.password_id = NULL.
     */
    @Override
    public void clearPasswordPointer(String name) {
        this.queryAsync(connection -> {
            this.clearPasswordPointer0(connection, name);
        });
    }

    void clearPasswordPointer0(Connection connection, String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(CLEAR_USERS_PASSWORD_ID_BY_NAME_SQL)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    /**
     * Update lastSuccessfulLogin by user name.
     */
    @Override
    public void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_LAST_LOGIN_BY_NAME_SQL)) {
                ps.setLong(1, lastSuccessfulLogin);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    /**
     * Update IP by UUID.
     */
    @Override
    public void updateIpByName(String name, String ip) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_IP_BY_NAME_SQL)) {
                ps.setString(1, ip);
                ps.setObject(2, name);
                ps.executeUpdate();
            }
        });
    }

    void updatePasswordByOwner0(Connection connection, String ownerName, Password password) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_PASSWORD_BY_OWNER_SQL())) {
            ps.setString(1, ownerName);
            ps.setString(2, password.getHashedPassword());
            ps.setShort(3, (short) (password.getHashId() & 0xFF));
            ps.setString(4, password.getSalt());
            ps.setShort(5, (short) (password.getMatcherId() & 0xFF));
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new AlixException("No password row found for owner: " + ownerName);
            }
        }
    }

    /**
     * Replace a password row by owner name.
     */
    @Override
    public void updatePasswordByOwner(String ownerName, Password password) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_PASSWORD_BY_OWNER_SQL())) {
                ps.setString(1, ownerName);
                ps.setString(2, password.getHashedPassword());
                ps.setShort(3, (short) (password.getHashId() & 0xFF));
                ps.setString(4, password.getSalt());
                ps.setShort(5, (short) (password.getMatcherId() & 0xFF));
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new AlixException("No password row found for owner: " + ownerName);
                }
            }
        });
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
                for (int i = 0; i < stackTrace.length; i++) {
                    var frame = stackTrace[i];
                    if (frame.getClassName().equals(this.getClass().getName())) {
                        var errAt = stackTrace[i - 1];
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