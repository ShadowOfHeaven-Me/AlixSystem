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

    //Unlike getTokenOrSupply() above, ALWAYS overwrites whatever LOCAL token was there before (the file/
    //in-memory map only - NOT the external database, see below) with the given value - used for an
    //explicit 2FA reset (lost/compromised device), never for the normal lazy-create-on-first-use path.
    //
    //Deliberately does NOT also write the external database here, unlike an earlier version of this
    //method: PersistentUserData#regenerateAuthToken() needs the new token committed to the database in
    //the SAME transaction as the player's re-encrypted email (see DatabaseUpdater#commitTokenAndEmail()),
    //so that a linked website's own periodic sync can never read the row in a split state (new token,
    //still-old-token-encrypted email, or vice versa) - a plain overwriteUserToken() call from here,
    //separate from the email write, couldn't guarantee that.
    public static void commitTokenLocally(Identity identity, String newToken) {
        tokenMap().put(identity.tokenKey(), newToken);
        save();
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