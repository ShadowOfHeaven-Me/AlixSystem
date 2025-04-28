package alix.common.utils.collections;

import java.security.SecureRandom;

public final class SecureRandomCharIterator extends RandomCharIterator {

    private final SecureRandom random;

    public SecureRandomCharIterator(char[] chars, SecureRandom random) {
        super(chars);
        this.random = random;
    }

    @Override
    public char next() {
        return this.chars[this.random.nextInt(this.chars.length)];
        //return chars[++index == size ? index = 0 : index];//we do not care for concurrency issues here, since this class is supposed to be pseudo-random
    }
}