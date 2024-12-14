package alix.common.database.migrate;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.security.password.Password;
import alix.common.data.security.password.hashing.Hashing;

import java.net.InetAddress;
import java.sql.ResultSet;

public final class NLoginSQLMigrateProvider implements MigrateSQLProvider {

    //Source code: https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/migrate/NLoginSQLMigrateReadProvider.java

    @Override
    public void migrateEntry(ResultSet rs) throws Exception {
        //var uniqueIdString = rs.getString("unique_id");
        var premiumIdString = rs.getString("mojang_id");
        var lastNickname = rs.getString("last_name");
        var lastLogin = rs.getTimestamp("last_login").toInstant().toEpochMilli();
        //var firstSeen = rs.getTimestamp("creation_date");
        var rawPassword = rs.getString("password");
        var ip = rs.getString("last_address");

        if (lastNickname == null) return; //|| uniqueIdString == null) return;

        Password password = Password.empty();

        if (rawPassword != null) {
            if (!rawPassword.startsWith("$SHA512$")) {
                AlixCommonMain.errorF("User %s has invalid algorithm %s, omitting", lastNickname, rawPassword);
                return;
            }
            var split = rawPassword.substring(8).split("\\$");
            password = Password.fromHashedMigrated(split[0], split[1], Hashing.SHA512_MIGRATE);
        }

        PersistentUserData data = UserFileManager.get(lastNickname);
        if (data == null) data = PersistentUserData.createDefault(lastNickname, InetAddress.getByName(ip), password);

        if (premiumIdString != null && !data.getPremiumData().getStatus().isPremium()) data.setPremiumData(PremiumData.createNew(premiumIdString));
        if(data.getLastSuccessfulLogin() < lastLogin) data.setLastSuccessfulLogin(lastLogin);
    }
}