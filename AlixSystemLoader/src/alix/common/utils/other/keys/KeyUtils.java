package alix.common.utils.other.keys;

import alix.common.utils.other.keys.factory.StrKeyFactory;
import alix.common.utils.other.keys.str.ArrayKey;

public final class KeyUtils {

    private static final StrKeyFactory strFactory = StrKeyFactory.createImpl();

    public static ArrayKey key(String s) {
        return strFactory.string2Key(s);
    }
}