package alix.common.utils.other.keys;

import alix.common.utils.other.keys.factory.KeyFactory;

public final class KeyUtils {

    private static final KeyFactory factory = KeyFactory.createImpl();

    public static ArrayKey key(String s) {
        return factory.string2Key(s);
    }
}