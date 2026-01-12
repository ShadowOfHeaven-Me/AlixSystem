package alix.common.database;

import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseConnector;
import alix.common.scheduler.AlixScheduler;

import java.sql.*;
import java.util.UUID;
import java.util.function.LongConsumer;

import static alix.common.database.QueryConstants.*;

final class DatabaseUpdaterImpl implements DatabaseUpdater {

    private final DatabaseConnector database;

    DatabaseUpdaterImpl(DatabaseConnector database) {
        this.database = database;
        this.database.connect();
    }

    @Override
    public void createTablesSync() {
        this.query(conn -> {
            try (var st = conn.createStatement()) {
                st.execute(CREATE_USERS_SQL);
                st.execute(CREATE_PASSWORDS_SQL);
                // Try to add FK once. Some DBs may throw if already added; ignore that case.
                try {
                    st.execute(ADD_FK_USERS_PASSWORD);
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

    @Override
    public void insertUser(String name, UUID uuid, long createdAt) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_USER_SQL)) {
                ps.setString(1, name);
                ps.setObject(2, uuid); // driver may accept UUID; otherwise use uuid.toString()
                ps.setLong(3, createdAt);
                ps.executeUpdate();
            }
        });
    }

    /**
     * Insert a password row and invoke onSuccess with the generated id.
     * If you don't care about the generated id, pass a no-op LongConsumer.
     */
    @Override
    public void addPassword(String ownerName, String hashedPassword, byte hashId, String salt, byte matcherId, LongConsumer onSuccess) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_PASSWORD_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, ownerName);
                ps.setString(2, hashedPassword);
                ps.setShort(3, (short) (hashId & 0xFF));
                ps.setString(4, salt);
                ps.setShort(5, (short) (matcherId & 0xFF));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        if (onSuccess != null) onSuccess.accept(id);
                    } else {
                        throw new SQLException("Insert password did not return generated id");
                    }
                }
            }
        });
    }

    /**
     * Atomically insert a password and link it to the user (update users.password_id).
     * onSuccess receives the generated password id.
     */
    @Override
    public void addPasswordAndLink(String ownerName, Password password) {
        this.queryAsync(connection -> {
            boolean origAuto = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                long pwdId;
                // insert password
                try (PreparedStatement ps = connection.prepareStatement(INSERT_PASSWORD_SQL, Statement.RETURN_GENERATED_KEYS)) {
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
                            throw new SQLException("Insert password did not return generated id");
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
                        throw new SQLException("User not found for ownerName: " + ownerName);
                    }
                }
                connection.commit();
                //if (onSuccess != null) onSuccess.accept(pwdId);
            } catch (SQLException e) {
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
        });
    }

    /**
     * Link an existing password id to a user, or clear it if passwordId == null.
     */
    @Override
    public void setPasswordPointer(String name, Long passwordId) {
        if (passwordId == null) {
            clearPasswordPointer(name);
            return;
        }
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERS_PASSWORD_ID_BY_NAME_SQL)) {
                ps.setLong(1, passwordId);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        });
    }

    /**
     * Explicit helper to set users.password_id = NULL.
     */
    @Override
    public void clearPasswordPointer(String name) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(CLEAR_USERS_PASSWORD_ID_BY_NAME_SQL)) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
        });
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

    /**
     * Replace a password row by password id.
     */
    @Override
    public void updatePasswordById(long passwordId, String hashedPassword, byte hashId, String salt, byte matcherId) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_PASSWORD_BY_ID_SQL)) {
                ps.setString(1, hashedPassword);
                ps.setShort(2, (short) (hashId & 0xFF));
                ps.setString(3, salt);
                ps.setShort(4, (short) (matcherId & 0xFF));
                ps.setLong(5, passwordId);
                ps.executeUpdate();
            }
        });
    }

    /**
     * Replace a password row by owner name.
     */
    @Override
    public void updatePasswordByOwner(String ownerName, Password password) {
        this.queryAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_PASSWORD_BY_OWNER_SQL)) {
                ps.setString(1, password.getHashedPassword());
                ps.setShort(2, (short) (password.getHashId() & 0xFF));
                ps.setString(3, password.getSalt());
                ps.setShort(4, (short) (password.getMatcherId() & 0xFF));
                ps.setString(5, ownerName);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("No password row found for owner: " + ownerName);
                }
            }
        });
    }

    void queryAsync(String query) {
        this.queryAsync(connection -> {
            try (var stmt = connection.prepareStatement(query)) {
                stmt.execute();
            }
        });
    }

    void queryAsync(ThrowableConsumer<Connection, Exception> func) {
        this.async(() -> this.query(func));
    }

    void query(ThrowableConsumer<Connection, Exception> func) {
        this.database.query(func);
    }

    void async(Runnable r) {
        AlixScheduler.asyncBlocking(r);
    }
}