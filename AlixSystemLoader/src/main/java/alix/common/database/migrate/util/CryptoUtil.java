package alix.common.database.migrate.util;

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

        //algo = "BCrypt-" + algo.toUpperCase();

        String password = algo + "$" + cost + "$" + salt + "$" + hash;
        return Password.fromBCryptMigrated(password);
    }

    public static String rawBcryptFromHashed(Password password) {
        /*var split = password.getHashedPassword().split("\\$");
        String cost = split[0];
        String hashedPassword = split[1];*/

        return password.getHashedPassword();
        /*"$%s$%s$%s%s".formatted(
                password.algo().replace("BCrypt-", "").toLowerCase(),
                cost,
                password.getSalt(),
                hashedPassword
        );*/
    }

}
