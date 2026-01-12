package alix.common.database;

interface QueryConstants {
    String CREATE_USERS_SQL =
            "CREATE TABLE IF NOT EXISTS alix_users (" +
            "  name TEXT PRIMARY KEY," +
            "  uuid UUID NOT NULL UNIQUE," +
            "  created_at BIGINT NOT NULL," +
            "  last_successful_login BIGINT," +
            "  ip VARCHAR(45)," +
            "  password_id BIGINT" +
            ")";

    String CREATE_PASSWORDS_SQL =
            "CREATE TABLE IF NOT EXISTS alix_passwords (" +
            "  id BIGSERIAL PRIMARY KEY," +
            "  owner_name TEXT NOT NULL UNIQUE REFERENCES alix_users(name) ON DELETE CASCADE," +
            "  hashed_password TEXT NOT NULL," +
            "  hash_id SMALLINT NOT NULL CHECK (hash_id BETWEEN 0 AND 255)," +
            "  salt TEXT," +
            "  matcher_id SMALLINT NOT NULL CHECK (matcher_id BETWEEN 0 AND 255)" +
            ")";

    String ADD_FK_USERS_PASSWORD =
            "ALTER TABLE alix_users " +
            "  ADD CONSTRAINT fk_alix_users_password " +
            "  FOREIGN KEY (password_id) REFERENCES alix_passwords(id) ON DELETE SET NULL";

    // Upsert user: if name exists overwrite uuid and created_at
    String INSERT_USER_SQL =
            "INSERT INTO alix_users (name, uuid, created_at) VALUES (?, ?, ?) " +
            "ON CONFLICT (name) DO UPDATE SET uuid = EXCLUDED.uuid, created_at = EXCLUDED.created_at";

    // Upsert password by owner_name; return id (either newly inserted or existing row's id)
    String INSERT_PASSWORD_SQL =
            "INSERT INTO alix_passwords (owner_name, hashed_password, hash_id, salt, matcher_id) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON CONFLICT (owner_name) DO UPDATE SET " +
            "  hashed_password = EXCLUDED.hashed_password, " +
            "  hash_id = EXCLUDED.hash_id, " +
            "  salt = EXCLUDED.salt, " +
            "  matcher_id = EXCLUDED.matcher_id " +
            "RETURNING id";

    // Update lastSuccessfulLogin
    String UPDATE_USERS_LAST_LOGIN_BY_NAME_SQL =
            "UPDATE alix_users SET last_successful_login = ? WHERE name = ?";

    // Update IP by name (changed as requested)
    String UPDATE_USERS_IP_BY_NAME_SQL =
            "UPDATE alix_users SET ip = ? WHERE name = ?";

    // Set password pointer by name (nullable)
    String UPDATE_USERS_PASSWORD_ID_BY_NAME_SQL =
            "UPDATE alix_users SET password_id = ? WHERE name = ?";

    String CLEAR_USERS_PASSWORD_ID_BY_NAME_SQL =
            "UPDATE alix_users SET password_id = NULL WHERE name = ?";

    String UPDATE_PASSWORD_BY_ID_SQL =
            "UPDATE alix_passwords SET hashed_password = ?, hash_id = ?, salt = ?, matcher_id = ? WHERE id = ?";

    String UPDATE_PASSWORD_BY_OWNER_SQL =
            "UPDATE alix_passwords SET hashed_password = ?, hash_id = ?, salt = ?, matcher_id = ? WHERE owner_name = ?";

    String SELECT_USER_WITH_PASSWORD_BY_NAME_SQL =
            "SELECT u.name, u.uuid, u.created_at, u.last_successful_login, u.ip, u.password_id, " +
            "p.id, p.hashed_password, p.hash_id, p.salt, p.matcher_id " +
            "FROM alix_users u LEFT JOIN alix_passwords p ON u.password_id = p.id WHERE u.name = ?";

}
