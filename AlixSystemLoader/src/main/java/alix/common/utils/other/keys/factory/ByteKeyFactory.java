package alix.common.utils.other.keys.factory;

import alix.common.utils.other.keys.str.ArrayKey;
import alix.common.utils.other.keys.str.ByteArray;
import sun.misc.Unsafe;

final class ByteKeyFactory implements StrKeyFactory {

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private final long valueOffset;

    ByteKeyFactory(long valueOffset) {
        this.valueOffset = valueOffset;
    }

    @Override
    public ArrayKey string2Key(String s) {
        return new ByteArray((byte[]) UNSAFE.getObject(s, this.valueOffset));
    }
}