package alix.common.utils.collections;

import alix.common.utils.other.throwable.AlixException;

import java.util.Random;

public class RandomCharIterator {

    private static final Random random = new Random();
    final char[] chars;
    //private final short size;
    //private short index;

    public RandomCharIterator(char[] chars) {
        this.chars = chars;
        int l = chars.length;
        if (l > 32767 || l == 0)
            throw new AlixException("Invalid LoopCharIterator chars length: " + l + " (max of 32767, min of 1).");
        //this.size = (short) l;
    }

    public char next() {
        return this.chars[random.nextInt(this.chars.length)];
        //return chars[++index == size ? index = 0 : index];//we do not care for concurrency issues here, since this class is supposed to be pseudo-random
    }

    public char[] next(int length) {
        char[] c = new char[length];
        //int newIndex = length + index;
        /*if (newIndex < size) {
            System.arraycopy(chars, index, c, 0, length);
            return c;
            //return Arrays.copyOfRange(chars, index, newIndex);
        }*/
        for (int i = 0; i < length; i++) c[i] = this.next();
        return c;
    }
}