package alix.common.utils.collections;

import java.security.SecureRandom;

public final class SecureRandomCharIterator extends RandomCharIterator {

    private static final SecureRandom random = new SecureRandom();

    public SecureRandomCharIterator(char[] chars) {
        super(chars);
    }

    @Override
    public char next() {
        return this.chars[random.nextInt(this.chars.length)];
        //return chars[++index == size ? index = 0 : index];//we do not care for concurrency issues here, since this class is supposed to be pseudo-random
    }
}