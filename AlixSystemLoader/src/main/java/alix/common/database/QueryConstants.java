package alix.common.database;

import alix.common.database.connect.DatabaseType;

interface QueryConstants {

    int MAIN_PASSWORD_SLOT = 0;
    int EXTRA_PASSWORD_SLOT = 1;

    String CREATE_USERS_SQL_MYSQL =
            "CREATE TABLE IF NOT EXISTS alix_users2 (" +
            "  name VARCHAR(16) NOT NULL," +
            "  uuid CHAR(36) NOT NULL," +
            "  created_at BIGINT NOT NULL," +
            "  last_successful_login BIGINT NULL," +
            "  ip VARCHAR(45) NULL," +
            "  muted_until BIGINT NOT NULL DEFAULT 0," +
            "  login_type VARCHAR(32) NOT NULL," +
            "  extra_login_type VARCHAR(32) NULL," +
            "  ip_auto_login BOOLEAN NULL," +
            "  auth_settings VARCHAR(32) NOT NULL," +
            "  has_proven_auth_access BOOLEAN NOT NULL DEFAULT FALSE," +
            "  identity VARCHAR(255) NOT NULL," +
            "  email TEXT NULL," +
            "  homes TEXT NULL," +
            "  premium_status TINYINT NOT NULL DEFAULT 0," +
            "  premium_uuid CHAR(36) NULL," +
            "  PRIMARY KEY (name)," +
            "  UNIQUE KEY uq_alix_users2_uuid (uuid)," +
            "  UNIQUE KEY uq_alix_users2_identity (identity)" +
            ") ENGINE=InnoDB;";

    String CREATE_USERS_SQL_POSTGRES =
            "CREATE TABLE IF NOT EXISTS alix_users2 (" +
            "  name VARCHAR(16) NOT NULL," +
            "  uuid UUID NOT NULL," +
            "  created_at BIGINT NOT NULL," +
            "  last_successful_login BIGINT NULL," +
            "  ip VARCHAR(45) NULL," +
            "  muted_until BIGINT NOT NULL DEFAULT 0," +
            "  login_type VARCHAR(32) NOT NULL," +
            "  extra_login_type VARCHAR(32) NULL," +
            "  ip_auto_login BOOLEAN NULL," +
            "  auth_settings VARCHAR(32) NOT NULL," +
            "  has_proven_auth_access BOOLEAN NOT NULL DEFAULT FALSE," +
            "  identity VARCHAR(255) NOT NULL," +
            "  email TEXT NULL," +
            "  homes TEXT NULL," +
            "  premium_status SMALLINT NOT NULL DEFAULT 0," +
            "  premium_uuid UUID NULL," +
            "  CONSTRAINT pk_alix_users2 PRIMARY KEY (name)," +
            "  CONSTRAINT uq_alix_users2_uuid UNIQUE (uuid)," +
            "  CONSTRAINT uq_alix_users2_identity UNIQUE (identity)," +
            "  CONSTRAINT chk_alix_users2_premium_status CHECK (premium_status IN (-1, 0, 1))" +
            ");";

    String CREATE_USERS_SQL_SQLITE =
            "CREATE TABLE IF NOT EXISTS alix_users2 (" +
            "  name TEXT NOT NULL PRIMARY KEY," +
            "  uuid TEXT NOT NULL UNIQUE," +
            "  created_at INTEGER NOT NULL," +
            "  last_successful_login INTEGER NULL," +
            "  ip TEXT NULL," +
            "  muted_until INTEGER NOT NULL DEFAULT 0," +
            "  login_type TEXT NOT NULL," +
            "  extra_login_type TEXT NULL," +
            "  ip_auto_login INTEGER NULL," +
            "  auth_settings TEXT NOT NULL," +
            "  has_proven_auth_access INTEGER NOT NULL DEFAULT 0," +
            "  identity TEXT NOT NULL UNIQUE," +
            "  email TEXT NULL," +
            "  homes TEXT NULL," +
            "  premium_status INTEGER NOT NULL DEFAULT 0 CHECK (premium_status IN (-1, 0, 1))," +
            "  premium_uuid TEXT NULL" +
            ");";

    String CREATE_PASSWORDS_SQL_MYSQL =
            "CREATE TABLE IF NOT EXISTS alix_passwords2 (" +
            "  owner_name VARCHAR(16) NOT NULL," +
            "  slot TINYINT NOT NULL," +
            "  hashed_password TEXT NULL," +
            "  hash_id TINYINT UNSIGNED NOT NULL," +
            "  salt TEXT NULL," +
            "  matcher_id TINYINT UNSIGNED NOT NULL," +
            "  PRIMARY KEY (owner_name, slot)," +
            "  CONSTRAINT fk_alix_passwords2_owner FOREIGN KEY (owner_name) REFERENCES alix_users2(name) ON DELETE CASCADE," +
            "  CONSTRAINT chk_alix_passwords2_slot CHECK (slot IN (0, 1))" +
            ") ENGINE=InnoDB;";

    String CREATE_PASSWORDS_SQL_POSTGRES =
            "CREATE TABLE IF NOT EXISTS alix_passwords2 (" +
            "  owner_name VARCHAR(16) NOT NULL," +
            "  slot SMALLINT NOT NULL," +
            "  hashed_password TEXT NULL," +
            "  hash_id SMALLINT NOT NULL CHECK (hash_id BETWEEN 0 AND 255)," +
            "  salt TEXT NULL," +
            "  matcher_id SMALLINT NOT NULL CHECK (matcher_id BETWEEN 0 AND 255)," +
            "  CONSTRAINT pk_alix_passwords2 PRIMARY KEY (owner_name, slot)," +
            "  CONSTRAINT fk_alix_passwords2_owner FOREIGN KEY (owner_name) REFERENCES alix_users2(name) ON DELETE CASCADE," +
            "  CONSTRAINT chk_alix_passwords2_slot CHECK (slot IN (0, 1))" +
            ");";

    String CREATE_PASSWORDS_SQL_SQLITE =
            "CREATE TABLE IF NOT EXISTS alix_passwords2 (" +
            "  owner_name TEXT NOT NULL," +
            "  slot INTEGER NOT NULL CHECK (slot IN (0, 1))," +
            "  hashed_password TEXT NULL," +
            "  hash_id INTEGER NOT NULL CHECK (hash_id BETWEEN 0 AND 255)," +
            "  salt TEXT NULL," +
            "  matcher_id INTEGER NOT NULL CHECK (matcher_id BETWEEN 0 AND 255)," +
            "  PRIMARY KEY (owner_name, slot)," +
            "  FOREIGN KEY (owner_name) REFERENCES alix_users2(name) ON DELETE CASCADE" +
            ");";

    String SELECT_USER_SQL =
            "SELECT " +
            "u.name, u.uuid, u.created_at, u.last_successful_login, u.ip, u.muted_until, " +
            "u.login_type, u.extra_login_type, u.ip_auto_login, u.auth_settings, " +
            "u.has_proven_auth_access, u.identity, u.email, u.homes, u.premium_status, u.premium_uuid, " +
            "p0.hashed_password, p0.hash_id, p0.salt, p0.matcher_id, " +
            "p1.hashed_password, p1.hash_id, p1.salt, p1.matcher_id " +
            "FROM alix_users2 u " +
            "LEFT JOIN alix_passwords2 p0 ON p0.owner_name = u.name AND p0.slot = 0 " +
            "LEFT JOIN alix_passwords2 p1 ON p1.owner_name = u.name AND p1.slot = 1 " +
            "WHERE u.name = ?";

    String UPSERT_USER_MYSQL =
            "INSERT INTO alix_users2 (" +
            "name, uuid, created_at, last_successful_login, ip, muted_until, " +
            "login_type, extra_login_type, ip_auto_login, auth_settings, " +
            "has_proven_auth_access, identity, email, homes, premium_status, premium_uuid" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "uuid = VALUES(uuid), " +
            "created_at = VALUES(created_at), " +
            "last_successful_login = VALUES(last_successful_login), " +
            "ip = VALUES(ip), " +
            "muted_until = VALUES(muted_until), " +
            "login_type = VALUES(login_type), " +
            "extra_login_type = VALUES(extra_login_type), " +
            "ip_auto_login = VALUES(ip_auto_login), " +
            "auth_settings = VALUES(auth_settings), " +
            "has_proven_auth_access = VALUES(has_proven_auth_access), " +
            "identity = VALUES(identity), " +
            "email = VALUES(email), " +
            "homes = VALUES(homes), " +
            "premium_status = VALUES(premium_status), " +
            "premium_uuid = VALUES(premium_uuid)";

    String UPSERT_USER_POSTGRES_AND_SQLITE =
            "INSERT INTO alix_users2 (" +
            "name, uuid, created_at, last_successful_login, ip, muted_until, " +
            "login_type, extra_login_type, ip_auto_login, auth_settings, " +
            "has_proven_auth_access, identity, email, homes, premium_status, premium_uuid" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (name) DO UPDATE SET " +
            "uuid = EXCLUDED.uuid, " +
            "created_at = EXCLUDED.created_at, " +
            "last_successful_login = EXCLUDED.last_successful_login, " +
            "ip = EXCLUDED.ip, " +
            "muted_until = EXCLUDED.muted_until, " +
            "login_type = EXCLUDED.login_type, " +
            "extra_login_type = EXCLUDED.extra_login_type, " +
            "ip_auto_login = EXCLUDED.ip_auto_login, " +
            "auth_settings = EXCLUDED.auth_settings, " +
            "has_proven_auth_access = EXCLUDED.has_proven_auth_access, " +
            "identity = EXCLUDED.identity, " +
            "email = EXCLUDED.email, " +
            "homes = EXCLUDED.homes, " +
            "premium_status = EXCLUDED.premium_status, " +
            "premium_uuid = EXCLUDED.premium_uuid";

    String UPSERT_PASSWORD_MYSQL =
            "INSERT INTO alix_passwords2 (" +
            "owner_name, slot, hashed_password, hash_id, salt, matcher_id" +
            ") VALUES (?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "hashed_password = VALUES(hashed_password), " +
            "hash_id = VALUES(hash_id), " +
            "salt = VALUES(salt), " +
            "matcher_id = VALUES(matcher_id)";

    String UPSERT_PASSWORD_POSTGRES_AND_SQLITE =
            "INSERT INTO alix_passwords2 (" +
            "owner_name, slot, hashed_password, hash_id, salt, matcher_id" +
            ") VALUES (?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (owner_name, slot) DO UPDATE SET " +
            "hashed_password = EXCLUDED.hashed_password, " +
            "hash_id = EXCLUDED.hash_id, " +
            "salt = EXCLUDED.salt, " +
            "matcher_id = EXCLUDED.matcher_id";

    String DELETE_PASSWORD_SQL =
            "DELETE FROM alix_passwords2 WHERE owner_name = ? AND slot = ?";

    String UPDATE_USERS_PREMIUM_SQL =
            "UPDATE alix_users2 SET premium_status = ?, premium_uuid = ? WHERE name = ?";

    String UPDATE_USERS_LAST_LOGIN_SQL =
            "UPDATE alix_users2 SET last_successful_login = ? WHERE name = ?";

    String UPDATE_USERS_IP_SQL =
            "UPDATE alix_users2 SET ip = ? WHERE name = ?";

    static String CREATE_USERS_SQL(DatabaseType type) {
        return switch (type) {
            case MYSQL -> CREATE_USERS_SQL_MYSQL;
            case POSTGRESQL -> CREATE_USERS_SQL_POSTGRES;
            case SQLITE -> CREATE_USERS_SQL_SQLITE;
        };
    }

    static String CREATE_PASSWORDS_SQL(DatabaseType type) {
        return switch (type) {
            case MYSQL -> CREATE_PASSWORDS_SQL_MYSQL;
            case POSTGRESQL -> CREATE_PASSWORDS_SQL_POSTGRES;
            case SQLITE -> CREATE_PASSWORDS_SQL_SQLITE;
        };
    }

    static String UPSERT_USER_SQL(DatabaseType type) {
        return switch (type) {
            case MYSQL -> UPSERT_USER_MYSQL;
            case POSTGRESQL, SQLITE -> UPSERT_USER_POSTGRES_AND_SQLITE;
        };
    }

    static String UPSERT_PASSWORD_SQL(DatabaseType type) {
        return switch (type) {
            case MYSQL -> UPSERT_PASSWORD_MYSQL;
            case POSTGRESQL, SQLITE -> UPSERT_PASSWORD_POSTGRES_AND_SQLITE;
        };
    }
}