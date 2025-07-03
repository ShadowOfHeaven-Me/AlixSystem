package alix.common.database.migrate;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.security.password.Password;
import alix.common.data.security.password.hashing.Hashing;
import alix.common.database.migrate.util.CryptoUtil;

import java.sql.ResultSet;

final class AuthMeSQLMigrateProvider implements MigrateProvider {

    @Override
    public void migrateEntry(ResultSet rs) throws Exception {
        var nickname = rs.getString("realname");
        var ip = rs.getString("ip");
        var passwordRaw = rs.getString("password");
        var lastSeen = rs.getLong("lastlogin");
        var firstSeen = rs.getLong("regdate");

        AlixCommonMain.logInfo("rs=" + rs);

        if (nickname == null || UserFileManager.hasName(nickname)) return;

        Password password = Password.empty();

        if (passwordRaw != null) {
            if (passwordRaw.startsWith("$SHA$")) {
                var split = passwordRaw.split("\\$");

                //var algo = "SHA-256";
                var salt = split[2];
                var hash = split[3];

                password = Password.fromHashedMigrated(hash, salt, Hashing.SHA256_MIGRATE);
            } else if (passwordRaw.startsWith("$2a$")) {
                password = CryptoUtil.convertFromBCryptRaw(passwordRaw);
            } else {
                this.error("User " + nickname + " has an invalid password hash, defaulting to no password (unregistered)");
            }
        }
        var data = PersistentUserData.createDefault(nickname, UNKNOWN_ADDRESS, password);
        data.setLastSuccessfulLogin(lastSeen);

        UserFileManager.putData(data);
        AlixCommonMain.logInfo("Migrated " + nickname + " (" + rs + ")");
    }
}
