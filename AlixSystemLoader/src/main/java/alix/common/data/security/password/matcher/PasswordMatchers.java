package alix.common.data.security.password.matcher;

import alix.common.data.security.password.Password;
import alix.common.data.security.password.hashing.Hashing;
import alix.common.data.security.password.hashing.HashingAlgorithm;
import alix.common.database.migrate.util.CryptoUtil;
import alix.common.utils.other.throwable.AlixError;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PasswordMatchers {

    //A plain String#equals() here would short-circuit on the first differing byte, making the comparison
    //time depend on how many leading characters of a guessed hash happen to match the real one - a
    //textbook (if hard to exploit over a real network's jitter) timing side-channel for password hashes.
    //MessageDigest.isEqual() is the standard constant-time byte-array comparison for exactly this case.
    private static boolean hashesMatch(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static final PasswordMatcher[] matchers = createMatchers();
    public static final PasswordMatcher ALIX_FORMAT = matchers[0];
    public static final PasswordMatcher MIGRATION_FORMAT = matchers[1];
    public static final PasswordMatcher BCRYPT_FORMAT = matchers[2];

    public static PasswordMatcher matcherOfId(byte id) {
        return matchers[id];
    }

    //The matcher for Alix passwords
    private static final class Matcher0 implements PasswordMatcher {

        @Override
        public boolean matches(Password password, String unhashedInput) {
            String hashedPassword = password.getHashedPassword();
            HashingAlgorithm algorithm = password.getHashing();
            String salt = password.getSalt();

            String hashedInput = this.hash(unhashedInput, algorithm, salt);
            //AlixCommonMain.logError("hashedInput='" + hashedInput + "' hashedPassword='" + hashedPassword + "'");

            return hashesMatch(hashedPassword, hashedInput);
        }

        @Override
        public String hash(String input, HashingAlgorithm algo, String salt) {
            return Hashing.hashSaltFirst(algo, input, salt);
        }

        @Override
        public byte matcherId() {
            return 0;
        }
    }

    //The matcher for imported passwords
    private static final class Matcher1 implements PasswordMatcher {

        @Override
        public boolean matches(Password password, String unhashedInput) {
            String hashedPassword = password.getHashedPassword();
            HashingAlgorithm algorithm = password.getHashing();
            String salt = password.getSalt();

            //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/crypto/MessageDigestCryptoProvider.java#L63
            String hashedInput = this.hash(unhashedInput, algorithm, salt);

            return hashesMatch(hashedPassword, hashedInput);
        }

        @Override
        public String hash(String input, HashingAlgorithm algo, String salt) {
            //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/crypto/MessageDigestCryptoProvider.java#L63
            //hash with salt, if has salt
            return !salt.isEmpty() ? algo.hash(algo.hash(input) + salt) : algo.hash(input);
        }

        @Override
        public byte matcherId() {
            return 1;
        }
    }

    //The matcher for imported BCrypt passwords
    private static final class Matcher2 implements PasswordMatcher {

        public static final BCrypt.Verifyer VERIFIER = BCrypt
                .verifyer(BCrypt.Version.VERSION_2A);

        @Override
        public boolean matches(Password password, String unhashedInput) {
            var raw = CryptoUtil.rawBcryptFromHashed(password).toCharArray();

            return VERIFIER.verify(unhashedInput.toCharArray(), raw).verified;
        }

        @Override
        public String hash(String input, HashingAlgorithm algo, String salt) {
            if (!algo.isBCrypt())
                throw new AlixError("how tf");

            return algo.hash(input);
        }

        @Override
        public byte matcherId() {
            return 2;
        }
    }

    private static PasswordMatcher[] createMatchers() {
        return new PasswordMatcher[]{
                new Matcher0(), new Matcher1(), new Matcher2()
        };
    }
}
