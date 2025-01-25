package alix.common.utils.other.keys.factory;

import alix.common.reflection.CommonReflection;
import alix.common.utils.other.AlixUnsafe;
import alix.common.utils.other.keys.str.ArrayKey;
import alix.common.utils.other.throwable.AlixError;

import java.lang.reflect.Field;

public interface StrKeyFactory {

    ArrayKey string2Key(String s);

    static StrKeyFactory createImpl() {
        Field f = CommonReflection.getDeclaredField(String.class, "value");
        long offset = AlixUnsafe.getUnsafe().objectFieldOffset(f);
        if (f.getType() == byte[].class) return new ByteKeyFactory(offset);
        if (f.getType() == char[].class) return new CharKeyFactory(offset);
        throw new AlixError("Unknown String value type: " + f.getType() + "!");
    }
}