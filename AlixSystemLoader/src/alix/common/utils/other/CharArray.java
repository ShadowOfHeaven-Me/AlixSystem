package alix.common.utils.other;

import java.util.Arrays;

public final class CharArray {

    private final char[] value;
    private final int hashCode;

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