package alix.common.utils.other.keys.factory;

import alix.common.utils.other.keys.str.ArrayKey;
import alix.common.utils.other.keys.str.CharArray;
import sun.misc.Unsafe;

final class CharKeyFactory implements StrKeyFactory {

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private final long valueOffset;

    CharKeyFactory(long valueOffset) {
        this.valueOffset = valueOffset;
    }

    @Override
    public ArrayKey string2Key(String s) {
        return new CharArray((char[]) UNSAFE.getObject(s, this.valueOffset));
    }
}