package alix.common.data.security.password.hashing;

import alix.common.data.security.password.matcher.PasswordMatcher;
import alix.common.data.security.password.matcher.PasswordMatchers;

public interface HashingAlgorithm {

    String hash(String s);

    byte hashId();

    default PasswordMatcher getMatcher() {
        if (this.isBCrypt())
            return PasswordMatchers.BCRYPT_FORMAT;

        if (this.isMigrate())
            return PasswordMatchers.MIGRATION_FORMAT;

        return PasswordMatchers.ALIX_FORMAT;
    }

    default boolean generateSalt() {
        return !this.isBCrypt();
    }

    default boolean isHashing() {
        return this.hashId() != 0;
    }

    default boolean isBCrypt() {
        return this == Hashing.BCRYPT;
    }

    default boolean isMigrate() {
        return this == Hashing.SHA256_MIGRATE || this == Hashing.SHA512_MIGRATE;
    }
}