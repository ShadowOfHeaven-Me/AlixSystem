package alix.common.database;

import alix.common.database.connect.DatabaseType;

interface QueryConstants {
    String CREATE_USERS_SQL_MYSQL =
            "CREATE TABLE IF NOT EXISTS alix_users (" +
            "  name VARCHAR(16) NOT NULL," +
            "  uuid CHAR(36) NOT NULL," +
            "  created_at BIGINT NOT NULL," +
            "  last_successful_login BIGINT NULL," +
            "  ip VARCHAR(45) NULL," +
            "  premium_data_pointer BIGINT NOT NULL DEFAULT 0," + // 0 = UNKNOWN, -1 = NON_PREMIUM, >0 -> premium_data.id
            "  password_id BIGINT NULL," +
            "  PRIMARY KEY (name)," +
            "  UNIQUE KEY uq_users_uuid (uuid)" +
            ") ENGINE=InnoDB;";

    String CREATE_USERS_SQL_POSTGRES =
            "CREATE TABLE IF NOT EXISTS alix_users (" +
            "  name VARCHAR(16) NOT NULL," +
            "  uuid UUID NOT NULL," +
            "  created_at BIGINT NOT NULL," +
            "  last_successful_login BIGINT," +
            "  ip VARCHAR(45)," +
            "  premium_data_pointer BIGINT NOT NULL DEFAULT 0," +
            "  password_id BIGINT," +
            "  CONSTRAINT pk_alix_users PRIMARY KEY (name)," +
            "  CONSTRAINT uq_users_uuid UNIQUE (uuid)" +
            ");";

    static String CREATE_USERS_SQL() {
        return (switch (type()) {
            case MYSQL, SQLITE -> CREATE_USERS_SQL_MYSQL;
            case POSTGRESQL -> CREATE_USERS_SQL_POSTGRES;
            //case SQLITE -> throw new AlixError();
        });
    }

    String CREATE_PREMIUM_DATA_SQL_MYSQL =
            "CREATE TABLE IF NOT EXISTS alix_premium_data (" +
            "  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "  premium_uuid CHAR(36) NOT NULL" +
            ") ENGINE=InnoDB;";

    String CREATE_PREMIUM_DATA_SQL_POSTGRES =
            "CREATE TABLE IF NOT EXISTS alix_premium_data (" +
            "  id BIGSERIAL PRIMARY KEY," +
            "  premium_uuid CHAR(36) NOT NULL" +
            ");";

    static String CREATE_PREMIUM_DATA_SQL() {
        return (switch (type()) {
            case MYSQL, SQLITE -> CREATE_PREMIUM_DATA_SQL_MYSQL;
            case POSTGRESQL -> CREATE_PREMIUM_DATA_SQL_POSTGRES;
            //case SQLITE -> throw new AlixError();
        });
    }

    static DatabaseType type() {
        return DatabaseUpdater.INSTANCE.getType();
    }

    static String CREATE_PASSWORDS_SQL() {
        return (switch (type()) {
            case MYSQL, SQLITE -> CREATE_PASSWORDS_SQL_MYSQL;
            case POSTGRESQL -> CREATE_PASSWORDS_SQL_POSTGRES;
            //case SQLITE -> throw new AlixError();
        });
    }
    // MySQL / SQLite variants
    String INSERT_USER_SQL_MYSQL =
            "INSERT INTO alix_users (name, uuid, created_at) VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE uuid = VALUES(uuid), created_at = VALUES(created_at)";

    String INSERT_PASSWORD_SQL_MYSQL =
            "INSERT INTO alix_passwords (owner_name, hashed_password, hash_id, salt, matcher_id) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "  hashed_password = VALUES(hashed_password), " +
            "  hash_id = VALUES(hash_id), " +
            "  salt = VALUES(salt), " +
            "  matcher_id = VALUES(matcher_id), " +
            "  id = LAST_INSERT_ID(id)";

    String UPDATE_PASSWORD_BY_OWNER_SQL_MYSQL =
            "INSERT INTO alix_passwords (owner_name, hashed_password, hash_id, salt, matcher_id) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "  hashed_password = VALUES(hashed_password), " +
            "  hash_id = VALUES(hash_id), " +
            "  salt = VALUES(salt), " +
            "  matcher_id = VALUES(matcher_id), " +
            "  id = LAST_INSERT_ID(id)";

    // PostgreSQL variants
    String INSERT_USER_SQL_POSTGRES =
            "INSERT INTO alix_users (name, uuid, created_at) VALUES (?, ?, ?) " +
            "ON CONFLICT (name) DO UPDATE SET uuid = EXCLUDED.uuid, created_at = EXCLUDED.created_at";

    String INSERT_PASSWORD_SQL_POSTGRES =
            "INSERT INTO alix_passwords (owner_name, hashed_password, hash_id, salt, matcher_id) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON CONFLICT (owner_name) DO UPDATE SET " +
            "  hashed_password = EXCLUDED.hashed_password, " +
            "  hash_id = EXCLUDED.hash_id, " +
            "  salt = EXCLUDED.salt, " +
            "  matcher_id = EXCLUDED.matcher_id " +
            "RETURNING id";

    String UPDATE_PASSWORD_BY_OWNER_SQL_POSTGRES =
            "INSERT INTO alix_passwords (owner_name, hashed_password, hash_id, salt, matcher_id) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON CONFLICT (owner_name) DO UPDATE SET " +
            "  hashed_password = EXCLUDED.hashed_password, " +
            "  hash_id = EXCLUDED.hash_id, " +
            "  salt = EXCLUDED.salt, " +
            "  matcher_id = EXCLUDED.matcher_id " +
            "RETURNING id";

    // Dispatcher methods (choose the proper SQL at runtime based on type())
    static String INSERT_USER_SQL() {
        return (switch (type()) {
            case MYSQL, SQLITE -> INSERT_USER_SQL_MYSQL;
            case POSTGRESQL -> INSERT_USER_SQL_POSTGRES;
        });
    }

    static String INSERT_PASSWORD_SQL() {
        return (switch (type()) {
            case MYSQL, SQLITE -> INSERT_PASSWORD_SQL_MYSQL;
            case POSTGRESQL -> INSERT_PASSWORD_SQL_POSTGRES;
        });
    }

    static String UPDATE_PASSWORD_BY_OWNER_SQL() {
        return (switch (type()) {
            case MYSQL, SQLITE -> UPDATE_PASSWORD_BY_OWNER_SQL_MYSQL;
            case POSTGRESQL -> UPDATE_PASSWORD_BY_OWNER_SQL_POSTGRES;
        });
    }

    String CREATE_PASSWORDS_SQL_MYSQL =
            "CREATE TABLE IF NOT EXISTS alix_passwords (" +
            "  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "  owner_name VARCHAR(255) NOT NULL," +
            "  hashed_password TEXT NOT NULL," +
            "  hash_id TINYINT UNSIGNED NOT NULL," +
            "  salt TEXT," +
            "  matcher_id TINYINT UNSIGNED NOT NULL," +
            "  UNIQUE KEY uq_passwords_owner (owner_name)," +
            "  CONSTRAINT fk_passwords_owner_user FOREIGN KEY (owner_name) REFERENCES alix_users(name) ON DELETE CASCADE" +
            ") ENGINE=InnoDB;";

    String CREATE_PASSWORDS_SQL_POSTGRES =
            "CREATE TABLE IF NOT EXISTS alix_passwords (" +
            "  id BIGSERIAL PRIMARY KEY," +
            "  owner_name VARCHAR(255) NOT NULL," +
            "  hashed_password TEXT NOT NULL," +
            "  hash_id SMALLINT NOT NULL CHECK (hash_id BETWEEN 0 AND 255)," +
            "  salt TEXT," +
            "  matcher_id SMALLINT NOT NULL CHECK (matcher_id BETWEEN 0 AND 255)," +
            "  CONSTRAINT uq_passwords_owner UNIQUE (owner_name)," +
            "  CONSTRAINT fk_passwords_owner_user FOREIGN KEY (owner_name) REFERENCES alix_users(name) ON DELETE CASCADE" +
            ");";

    String ADD_FK_USERS_PASSWORD =
            "ALTER TABLE alix_users " +
            "  ADD CONSTRAINT fk_alix_users_password " +
            "  FOREIGN KEY (password_id) REFERENCES alix_passwords(id) ON DELETE SET NULL";


    String INSERT_PREMIUM_DATA_SQL =
            "INSERT INTO alix_premium_data (premium_uuid) VALUES (?)";

    String UPDATE_USERS_PREMIUM_POINTER_BY_NAME_SQL =
            "UPDATE alix_users SET premium_data_pointer = ? WHERE name = ?";

    String UPDATE_USERS_LAST_LOGIN_BY_NAME_SQL =
            "UPDATE alix_users SET last_successful_login = ? WHERE name = ?";

    String UPDATE_USERS_IP_BY_NAME_SQL =
            "UPDATE alix_users SET ip = ? WHERE name = ?";

    String UPDATE_USERS_PASSWORD_ID_BY_NAME_SQL =
            "UPDATE alix_users SET password_id = ? WHERE name = ?";

    String CLEAR_USERS_PASSWORD_ID_BY_NAME_SQL =
            "UPDATE alix_users SET password_id = NULL WHERE name = ?";

    String UPDATE_PASSWORD_BY_ID_SQL =
            "UPDATE alix_passwords SET hashed_password = ?, hash_id = ?, salt = ?, matcher_id = ? WHERE id = ?";

    String SELECT_USER_WITH_PASSWORD_BY_NAME_SQL =
            "SELECT u.name, u.uuid, u.created_at, u.last_successful_login, u.ip, u.password_id, " +
            "p.id, p.hashed_password, p.hash_id, p.salt, p.matcher_id " +
            "FROM alix_users u LEFT JOIN alix_passwords p ON u.password_id = p.id WHERE u.name = ?";

}
