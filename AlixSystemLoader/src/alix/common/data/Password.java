package alix.common.data;


import alix.common.utils.AlixCommonUtils;
import alix.common.utils.collections.LoopCharIterator;
import alix.common.utils.security.Hashing;
import alix.common.utils.security.HashingAlgorithm;

import java.util.Random;

public final class Password {

    private static final LoopCharIterator loopCharIterator;
    private String hashedPassword;
    private HashingAlgorithm hashingAlgorithm;
    private byte hashId;

    private Password() {
        this.hashId = Hashing.CONFIG_HASH_ID;
        this.hashingAlgorithm = Hashing.ofHashId(hashId);
    }

    private Password(String hashedPassword, byte hashId) {
        this.hashedPassword = hashedPassword;
        this.hashId = hashId;
        this.hashingAlgorithm = Hashing.ofHashId(hashId);
    }

    public void setPassword(String unhashedPassword) {
        this.hashId = Hashing.CONFIG_HASH_ID;
        this.hashingAlgorithm = Hashing.ofHashId(hashId);
        this.hashedPassword = hashingAlgorithm.hash(unhashedPassword);
    }

    public void setFrom(Password password) {
        this.hashedPassword = password.hashedPassword;
        this.hashId = password.hashId;
        this.hashingAlgorithm = password.hashingAlgorithm;
    }

    public String toSavable() {
        return hashedPassword + ":" + hashId;
    }

    public boolean isCorrect(String unhashedPassword) {
        return this.hashedPassword.equals(hashingAlgorithm.hash(unhashedPassword));
    }

/*    public boolean isCorrectLiteral(String hashedPassword) {
        return this.hashedPassword.equals(hashedPassword);
    }*/

    public String getHashedPassword() {
        return hashedPassword;
    }

    public final boolean isHashed() {
        return hashId != 0;
    }

    public boolean isSet() {
        return hashedPassword != null;
    }

    public HashingAlgorithm getHashingAlgorithm() {
        return hashingAlgorithm;
    }

    public final void reset() {
        this.hashedPassword = null;
        this.hashId = Hashing.CONFIG_HASH_ID;
        this.hashingAlgorithm = Hashing.ofHashId(hashId);
    }

    public static Password createRandomUnhashed() {
        Random r = AlixCommonUtils.random;

        int length = 5 + r.nextInt(8);//min char length + 8 because 2^n results in a faster generation

        return new Password(new String(loopCharIterator.next(length)), (byte) 0);
    }

/*    public static Password generatePseudoRandom(String generationBase) {
        byte hashId = 1;
        return new Password(ofHashId(hashId).hash(generationBase), hashId);
    }*/

    public static Password newEmpty() {
        return new Password();
    }

    public static Password readFromSaved(String savablePassword) {
        String[] s = savablePassword.split(":");

        String password = s[0];

        if (password.equals("null")) password = null;

        if (s.length == 1) return new Password(password, Hashing.CONFIG_HASH_ID);

        return new Password(password, Byte.parseByte(s[1]));
    }

    static {
        char[] generationChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-+=".toCharArray();
        loopCharIterator = new LoopCharIterator(AlixCommonUtils.shuffle(generationChars));
    }
}