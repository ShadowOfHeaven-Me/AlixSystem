/*
package alix.common.data.security.password.hashing;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class HashProviders {

    //Source code: https://github.com/kyngs/LibreLogin/blob/5cae5bd01fa37e45ee1d529bfa5c8eb1dcdc5c58/Plugin/src/main/java/xyz/kyngs/librelogin/common/crypto/BCrypt2ACryptoProvider.java#L16

    public static final BCrypt.Hasher HASHER = BCrypt
            .with(BCrypt.Version.VERSION_2A);
    public static final BCrypt.Verifyer VERIFIER = BCrypt
            .verifyer(BCrypt.Version.VERSION_2A);

    @Override
    @Nullable
    public static HashedPassword createHash(String password) {
        String hash;
        try {
            hash = HASHER.hashToString(10, password.toCharArray());
        } catch (IllegalArgumentException e) {
            return null;
        }
        return CryptoUtil.convertFromBCryptRaw(
                hash
        );
    }

    @Override
    public boolean matches(String input, HashedPassword password) {
        var raw = CryptoUtil.rawBcryptFromHashed(password).toCharArray();
        BCrypt.Result result;
        try {
            result = VERIFIER.verify(input.toCharArray(),
                    raw
            );
        } catch (IllegalArgumentException e) {
            return false;
        }

        return result.verified;
    }

}*/
