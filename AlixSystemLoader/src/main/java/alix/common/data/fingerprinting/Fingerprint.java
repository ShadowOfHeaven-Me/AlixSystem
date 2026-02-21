package alix.common.data.fingerprinting;

import alix.common.data.security.password.Password;

public final class Fingerprint {

    private final Password hash;

    Fingerprint(FingerprintBuilder builder) {
        this.hash = Password.commonHash(Integer.toBinaryString(builder.value));
    }

    /*public String toSavable() {
        return Integer.toHexString(val);
    }*/
}