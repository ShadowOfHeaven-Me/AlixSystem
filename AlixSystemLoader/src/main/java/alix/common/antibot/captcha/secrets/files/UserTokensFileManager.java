package alix.common.antibot.captcha.secrets.files;

import alix.common.data.Identity;
import alix.common.database.DatabaseUpdater;
import alix.common.login.auth.GoogleAuthUtils;
import alix.common.utils.other.keys.secret.MapSecretKey;

import java.util.Map;
import java.util.function.Supplier;

public final class UserTokensFileManager {

    private static final UserTokensFile file = new UserTokensFile();
    private static final DatabaseUpdater database = DatabaseUpdater.INSTANCE;

    public static String getTokenOrSupply(Identity identity) {
        return getTokenOrSupply0(identity, GoogleAuthUtils::generateSecretKey);
    }

    //Unlike getTokenOrSupply() above, ALWAYS overwrites whatever token was there before (locally and in the
    //external database) with the GIVEN value - used for an explicit 2FA reset (lost/compromised device),
    //never for the normal lazy-create-on-first-use path. Split out from generating the token (see
    //PersistentUserData#regenerateAuthToken()) so a caller that also needs to re-encrypt something keyed
    //off the old token (namely the player's email) can do that re-encryption FIRST, using the new value,
    //and only commit it here once that's known to have succeeded - never leaving a token committed that
    //something else failed to actually adopt.
    public static void commitToken(Identity identity, String newToken) {
        tokenMap().put(identity.tokenKey(), newToken);
        save();
        database.overwriteUserToken(identity, newToken);
    }

    static String getTokenOrSupply0(Identity identity, Supplier<String> tokenSupplier) {
        return tokenMap().computeIfAbsent(identity.tokenKey(), k -> {
            var token = tokenSupplier.get();
            database.saveUserToken(identity, token);
            return token;
        });
    }

    private static Map<MapSecretKey, String> tokenMap() {
        return file.getMap();
    }

    public static void save() {
        file.save();
    }

    static {
        file.loadExceptionless();
        database.loadAllTokens(tokenMap()).thenRun(UserTokensFileManager::save);//missing should be added in UserFileManager
    }

    public static void init() {
    }
}