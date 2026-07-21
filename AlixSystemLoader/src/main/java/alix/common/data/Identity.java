package alix.common.data;

import java.security.SecureRandom;

public final class Identity {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final String identity;

    Identity(String identity) {
        this.identity = identity;
    }

    public String identity() {
        return identity;
    }

    public static Identity newIdentity(String name) {
        return new Identity(name + "*" + Long.toHexString(RANDOM.nextLong()));
    }

    public static Identity fromSaved(String name, String saved) {
        return new Identity(saved.equals("0") ? name : saved);
    }
}