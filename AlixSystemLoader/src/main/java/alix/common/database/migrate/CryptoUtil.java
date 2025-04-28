package alix.common.database.migrate;

import alix.common.data.security.password.Password;

public final class CryptoUtil {

    //Source code: https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/util/CryptoUtil.java#L17

    //migration
    public static Password convertFromBCryptRaw(String raw) {
        var split = raw.split("\\$");
        var algo = split[1];
        var cost = split[2];
        var rest = split[3];

        var salt = rest.substring(0, 22);
        var hash = rest.substring(22);

        algo = "BCrypt-" + algo.toUpperCase();

        return null;//Password.fromHashedMigrated()
    }

    public static String rawBcryptFromHashed(Password password) {
        var split = password.getHashedPassword().split("\\$");
        String cost = split[0];
        String hashedPassword = split[1];

        return null;/*"$%s$%s$%s%s".formatted(
                password.algo().replace("BCrypt-", "").toLowerCase(),
                cost,
                password.getSalt(),
                hashedPassword
        );*/
    }

}
