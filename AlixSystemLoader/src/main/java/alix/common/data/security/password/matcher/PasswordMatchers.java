package alix.common.data.security.password.matcher;

import alix.common.data.security.password.Password;
import alix.common.data.security.password.hashing.Hashing;
import alix.common.data.security.password.hashing.HashingAlgorithm;
import alix.common.database.migrate.CryptoUtil;
import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordMatchers {

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

            String hashedInput = Hashing.hashSaltFirst(algorithm, unhashedInput, salt);
            //AlixCommonMain.logError("hashedInput='" + hashedInput + "' hashedPassword='" + hashedPassword + "'");

            return hashedPassword.equals(hashedInput);
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
            String hashedInput = password.hasSalt() ? algorithm.hash(algorithm.hash(unhashedInput) + salt) : algorithm.hash(unhashedInput);

            return hashedPassword.equals(hashedInput);
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
