package alix.common.login.auth;

import java.security.SecureRandom;

//8 single-use backup codes generated alongside a player's Google Authenticator secret, the same shape and
//purpose as Azuriom's native 2FA recovery codes (see TwoFactorAuthenticatable::generateRecoveryCodes() on
//the website side) - a way back in when the authenticator app/device is lost, without needing a brand new
//secret (and therefore a brand new QR code every party involved has to re-scan) just to recover access.
//Stored (see PersistentUserData/DatabaseUpdater) as plaintext, same tradeoff the token/email columns
//already make - see QueryConstants#UPDATE_RECOVERY_CODES_SQL for why: a player must be able to view them
//again later, not just once at generation time.
public final class RecoveryCodes {

    private static final SecureRandom RANDOM = new SecureRandom();
    //Excludes 0/O and 1/I - a hand-copied or read-aloud code shouldn't depend on telling those apart.
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_COUNT = 8;
    private static final int GROUP_LENGTH = 5;
    private static final char SEPARATOR = ',';

    public static String[] generate() {
        String[] codes = new String[CODE_COUNT];
        for (int i = 0; i < CODE_COUNT; i++) codes[i] = generateOne();
        return codes;
    }

    private static String generateOne() {
        return group() + "-" + group();
    }

    private static String group() {
        StringBuilder sb = new StringBuilder(GROUP_LENGTH);
        for (int i = 0; i < GROUP_LENGTH; i++) sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    public static String join(String[] codes) {
        return String.join(String.valueOf(SEPARATOR), codes);
    }

    public static String[] split(String joined) {
        if (joined == null || joined.isEmpty()) return new String[0];
        return joined.split(String.valueOf(SEPARATOR));
    }

    //Case-insensitive, whitespace-tolerant - matches how a player is likely to type a code back in chat.
    public static boolean matches(String storedCode, String typedCode) {
        if (storedCode == null || typedCode == null) return false;
        return storedCode.trim().equalsIgnoreCase(typedCode.trim());
    }

    private RecoveryCodes() {
    }
}
