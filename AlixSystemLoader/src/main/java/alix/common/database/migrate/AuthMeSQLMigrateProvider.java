package alix.common.database.migrate;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.security.password.Password;
import alix.common.data.security.password.hashing.Hashing;
import alix.common.database.migrate.util.CryptoUtil;

import java.net.InetAddress;
import java.sql.ResultSet;

final class AuthMeSQLMigrateProvider implements MigrateProvider {

    @Override
    public void migrateEntry(ResultSet rs) throws Exception {
        var nickname = rs.getString("realname");
        var ip = rs.getString("ip");
        var passwordRaw = rs.getString("password");
        var lastSeen = rs.getLong("lastlogin");
        var firstSeen = rs.getLong("regdate");

        if (nickname == null) {
            AlixCommonMain.logInfo("Skipping entry - no nickname");
            return;
        }

        if (UserFileManager.hasName(nickname)) {
            AlixCommonMain.logInfo("Skipping entry " + nickname + " - already exists within Alix");
            return;
        }

        var sb = new StringBuilder();
        Password password = Password.empty();

        if (passwordRaw != null) {
            if (passwordRaw.startsWith("$SHA$")) {
                var split = passwordRaw.split("\\$");

                //var algo = "SHA-256";
                var salt = split[2];
                var hash = split[3];

                password = Password.fromHashedMigrated(hash, salt, Hashing.SHA256_MIGRATE);
                sb.append("SHA256 Password '").append(passwordRaw).append("'");
            } else if (passwordRaw.startsWith("$2a$")) {
                password = CryptoUtil.convertFromBCryptRaw(passwordRaw);
                sb.append("BCrypt Password '").append(passwordRaw).append("'");
            } else {
                this.error("User " + nickname + " has an invalid password hash, defaulting to no password (unregistered)");
            }
        } else sb.append("No Password - unregistered");

        InetAddress addr = ip == null ? UNKNOWN_ADDRESS : InetAddress.getByName(ip);

        sb.append(" with ").append(ip == null ? "unknown" : "known").append(" ip");
        if (ip != null)
            sb.append("=").append(ip);

        var data = PersistentUserData.createDefault(nickname, addr, password);
        data.setLastSuccessfulLogin(lastSeen);

        sb.append(", lastLogin=").append((System.currentTimeMillis() - lastSeen) / 1000).append(" seconds ago");

        UserFileManager.putData(data);
        AlixCommonMain.logInfo("Migrated " + nickname + " " + sb);
    }
}
