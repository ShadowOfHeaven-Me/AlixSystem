package alix.common.data.security.password;

import alix.common.AlixCommonMain;
import alix.common.scheduler.AlixScheduler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Checks a plaintext password against the HaveIBeenPwned breached-password database, using their
 * k-anonymity range API (https://haveibeenpwned.com/API/v3#PwnedPasswords): only the first 5 characters
 * of the password's SHA-1 hash are ever sent over the network - the real password, and even its full
 * hash, never leave this server. Gated behind 'check-breached-passwords' in config.yml (default off,
 * since enabling it adds a network round-trip - up to CONNECT_TIMEOUT + REQUEST_TIMEOUT worst case - to
 * every registration/password change). See AlixCommonUtils#getPasswordInvalidityReasonAsync for the call site.
 * <p>
 * Deliberately fails OPEN (reports false, i.e. "not breached") on any error - a DNS hiccup, a timeout, an
 * HTTP error, a malformed response - rather than blocking registration because a third-party service is
 * unreachable. The only thing this check can ever do is add friction for a password that's already known
 * to be publicly breached; it must never be the reason a legitimate player can't register at all.
 */
public final class HibpChecker {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    /**
     * @param password the plaintext password to check - never sent anywhere; only a 5-character prefix
     *                  of its SHA-1 hash is
     * @param callback receives true if this password appears in the HaveIBeenPwned breach corpus, false if
     *                 it doesn't OR if the check itself failed for any reason (see class docs - this fails
     *                 open). Invoked on AlixScheduler's blocking-task executor - i.e. never on whatever
     *                 thread called isBreachedAsync() itself, and never the calling thread blocked waiting
     *                 for it either.
     */
    public static void isBreachedAsync(String password, Consumer<Boolean> callback) {
        AlixScheduler.asyncBlocking(() -> callback.accept(isBreachedBlocking(password)));
    }

    //The actual blocking HTTP call - only ever invoked on AlixScheduler's asyncBlocking executor, see isBreachedAsync().
    private static boolean isBreachedBlocking(String password) {
        try {
            String hash = sha1Hex(password);
            String prefix = hash.substring(0, 5);
            String suffix = hash.substring(5);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.pwnedpasswords.com/range/" + prefix))
                    .timeout(REQUEST_TIMEOUT)
                    .header("User-Agent", "AlixSystem")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                AlixCommonMain.logWarning("HaveIBeenPwned check returned HTTP " + response.statusCode() + " - skipping the breached-password check for this attempt (failing open).");
                return false;
            }

            //response body is lines of "<35-char suffix>:<times seen>\r\n", covering every hash sharing our 5-char prefix
            for (String line : response.body().split("\r\n")) {
                int colon = line.indexOf(':');
                if (colon < 0) continue;
                if (line.substring(0, colon).equalsIgnoreCase(suffix)) return true;
            }
            return false;
        } catch (Exception e) {
            AlixCommonMain.logWarning("Could not reach the HaveIBeenPwned API to check a password (failing open, the password is allowed): " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static String sha1Hex(String s) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(40);
        for (byte b : digest) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    private HibpChecker() {
    }
}
