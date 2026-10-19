package alix.common.antibot.captcha.secrets.files;

import alix.common.data.Identity;
import alix.common.database.DatabaseUpdater;
import alix.common.database.file.DatabaseConfig;
import alix.common.login.auth.GoogleAuthUtils;
import alix.common.utils.other.keys.secret.MapSecretKey;

import java.util.Map;
import java.util.function.Supplier;

public final class UserTokensFileManager {

    private static final UserTokensFile file = new UserTokensFile();
    private static final DatabaseUpdater database = DatabaseUpdater.INSTANCE;
    //database.yml's refresh-token-on-login - checked here rather than at each of refreshFromDatabase()'s
    //two call sites, so the "don't refetch" opt-out actually applies to both, including the one that
    //piggybacks on a query already happening for other reasons.
    private static final boolean refreshOnLogin = DatabaseConfig.EXTERNAL.getBoolean("refresh-token-on-login");

    public static String getTokenOrSupply(Identity identity) {
        return getTokenOrSupply0(identity, GoogleAuthUtils::generateSecretKey);
    }

    //The in-memory/file token cache is only ever bulk-loaded ONCE, at plugin startup (see the static
    //initializer below) - unlike PersistentUserData/LoginParams, it has no per-connection refresh of its
    //own. Without this, an account whose token was created or changed by an external source (a linked
    //website's own 2FA setup, for instance) while this server was already running would silently be
    //invisible to getTokenOrSupply()'s cache-miss path, which would then GENERATE A BRAND NEW, DIFFERENT
    //token and overwrite the real one in the database - permanently desyncing the account from whatever
    //device the player actually scanned the QR code with on the website. Call this once per login (see
    //ua.nanit.limbo.integration.LimboIntegration#onLoginStart()) before anything else touches the token,
    //so the cache is guaranteed fresh for every downstream use (the login-time app-code prompt, email
    //decryption, and every Account Settings GUI action alike).
    //
    //Async and non-blocking (a real DB round-trip) - onComplete runs once the check is done, whether or
    //not anything was actually found/updated; callers are responsible for hopping back onto whatever
    //thread they need afterward (this may complete on a background DB thread).
    //
    //No-ops (runs onComplete immediately, synchronously) unless refresh-token-on-login is actually enabled
    //in database.yml - this is an EXTRA per-login database request on top of whatever caching-strategy
    //already does, so it must stay opt-in rather than firing just because a real external database happens
    //to be configured.
    public static void refreshFromDatabase(Identity identity, Runnable onComplete) {
        if (!refreshOnLogin) {
            onComplete.run();
            return;
        }
        database.loadToken(identity, token -> {
            if (token != null) tokenMap().put(identity.tokenKey(), token);
            onComplete.run();
        });
    }

    //Unlike getTokenOrSupply() above, ALWAYS overwrites whatever LOCAL token was there before (the file/
    //in-memory map only - NOT the external database, see below) with the given value - used for an
    //explicit 2FA reset (lost/compromised device), never for the normal lazy-create-on-first-use path.
    //
    //Deliberately does NOT also write the external database here, unlike an earlier version of this
    //method: PersistentUserData#regenerateAuthToken() needs the new token committed to the database in
    //the SAME transaction as the player's re-encrypted email (see DatabaseUpdater#commitTokenAndEmail()),
    //so that a linked website's own periodic sync can never read the row in a split state (new token,
    //still-old-token-encrypted email, or vice versa) - a plain token-only write from here, separate from
    //the email write, couldn't guarantee that.
    public static void commitTokenLocally(Identity identity, String newToken) {
        tokenMap().put(identity.tokenKey(), newToken);
        save();
    }

    //Removes this player's LOCAL token cache entry (file/in-memory map only) - paired with
    //DatabaseUpdater#removeUserToken() for a full account-data wipe (see UserFileManager#remove()).
    public static void removeTokenLocally(Identity identity) {
        tokenMap().remove(identity.tokenKey());
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