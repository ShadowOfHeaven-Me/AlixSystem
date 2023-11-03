package alix.common.utils.collections;

public class LoopCharIterator {

    private final char[] chars;
    private final short size;
    private short index;

    public LoopCharIterator(char[] chars) {
        this.chars = chars;
        int l = chars.length;
        if (l > 32767 || l == 0)
            throw new RuntimeException("Invalid LoopCharIterator chars length: " + l + " (max of 32767, min of 1).");
        this.size = (short) l;
    }

    private char next() {
        if (++index == size) index = 0;
        return chars[index];
    }

    public char[] next(int length) {
        char[] c = new char[length];
/*        int newIndex = length + index;
        if (newIndex < size) {
            System.arraycopy(chars, index, c, 0, length);
            return c;
            //return Arrays.copyOfRange(chars, index, newIndex);
        }*/
        for (int i = 0; i < length; i++) c[i] = next();
        return c;
    }
}