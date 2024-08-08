package alix.common.utils.other.keys;

import java.util.Arrays;

public final class CharArray implements ArrayKey {

    private final char[] value;
    private final int hashCode;

    //Serves as a fast Map key substitute for String
    public CharArray(char[] value) {
        this.value = value;
        this.hashCode = Arrays.hashCode(this.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CharArray && Arrays.equals(this.value, ((CharArray) obj).value);
    }
}