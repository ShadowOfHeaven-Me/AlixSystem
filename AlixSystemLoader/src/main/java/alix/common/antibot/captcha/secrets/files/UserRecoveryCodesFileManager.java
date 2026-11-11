package alix.common.antibot.captcha.secrets.files;

import alix.common.data.Identity;
import alix.common.database.DatabaseUpdater;
import alix.common.login.auth.RecoveryCodes;
import alix.common.utils.other.keys.secret.MapSecretKey;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

//Recovery codes previously only ever went through DatabaseUpdater, which is a true no-op
//(NoOPDatabaseImpl) unless database.yml's external-sync is on - so on a standalone server (the common
//case, no external website sync) regenerateRecoveryCodes() handed the player a set of codes that were
//never actually persisted anywhere, and loadRecoveryCodes()/tryConsumeRecoveryCode() could never see or
//consume them again. Mirrors UserTokensFileManager's existing local-file-is-authoritative pattern (the
//2FA token itself already works this way) so recovery codes survive with or without a database.
public final class UserRecoveryCodesFileManager {

    private static final UserRecoveryCodesFile file = new UserRecoveryCodesFile();
    private static final DatabaseUpdater database = DatabaseUpdater.INSTANCE;

    public static void saveRecoveryCodesLocally(Identity identity, String joinedCodes) {
        codesMap().put(identity.tokenKey(), joinedCodes);
        save();
    }

    public static String loadRecoveryCodesLocally(Identity identity) {
        return codesMap().get(identity.tokenKey());
    }

    //Atomic read-check-write via ConcurrentHashMap#compute() on this identity's own key - same guarantee
    //DatabaseUpdaterImpl#tryConsumeRecoveryCode() gives for the database path (a second concurrent attempt
    //can't race the first, it's simply queued behind this key's compute()).
    public static boolean tryConsumeRecoveryCodeLocally(Identity identity, String typedCode) {
        AtomicBoolean matched = new AtomicBoolean(false);
        codesMap().compute(identity.tokenKey(), (k, joined) -> {
            String[] codes = RecoveryCodes.split(joined);
            int matchIndex = -1;
            for (int i = 0; i < codes.length; i++) {
                if (RecoveryCodes.matches(codes[i], typedCode)) {
                    matchIndex = i;
                    break;
                }
            }
            if (matchIndex < 0) return joined;

            matched.set(true);
            String[] remaining = new String[codes.length - 1];
            System.arraycopy(codes, 0, remaining, 0, matchIndex);
            System.arraycopy(codes, matchIndex + 1, remaining, matchIndex, codes.length - matchIndex - 1);
            return RecoveryCodes.join(remaining);
        });

        if (matched.get()) save();
        return matched.get();
    }

    //Paired with DatabaseUpdater#removeUserToken() for a full account-data wipe (see UserFileManager#remove()).
    public static void removeRecoveryCodesLocally(Identity identity) {
        codesMap().remove(identity.tokenKey());
        save();
    }

    private static Map<MapSecretKey, String> codesMap() {
        return file.getMap();
    }

    public static void save() {
        file.save();
    }

    static {
        file.loadExceptionless();
        //Backfills codes that only exist in an external database (external-sync), same as
        //UserTokensFileManager does for tokens - a true no-op when running without one.
        database.loadAllRecoveryCodes(codesMap()).thenRun(UserRecoveryCodesFileManager::save);
    }

    public static void init() {
    }
}
