package alix.common.utils.other.keys.str;

import java.util.Arrays;

public final class ByteArray implements ArrayKey {

    private final byte[] value;
    private final int hashCode;

    //Serves as a fast Map key substitute for String
    public ByteArray(byte[] value) {
        this.value = value;
        this.hashCode = Arrays.hashCode(this.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ByteArray && Arrays.equals(this.value, ((ByteArray) obj).value);
    }
}