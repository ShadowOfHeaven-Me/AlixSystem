package alix.common.data;


import alix.common.data.security.Hashing;
import alix.common.data.security.HashingAlgorithm;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.collections.LoopCharIterator;

public final class Password {

    private static final LoopCharIterator loopCharIterator;
    private final String hashedPassword;
    private final HashingAlgorithm hashing;
    //private final String savablePassword;

    private Password(String hashedPassword, byte hashId) {
        this.hashedPassword = hashedPassword;
        this.hashing = Hashing.ofHashId(hashId);
    }

    private Password(String hashedPassword, HashingAlgorithm hashing) {
        this.hashedPassword = hashedPassword;
        this.hashing = hashing;
    }

    public String toSavable() {
        return hashedPassword + ":" + hashing.hashId();
    }

    public boolean isEqualTo(String unhashedPassword) {
        return this.hashedPassword.equals(this.hashing.hash(unhashedPassword));
    }

/*    public boolean isCorrectLiteral(String hashedPassword) {
        return this.hashedPassword.equals(hashedPassword);
    }*/

    public boolean isHashed() {
        return hashing.hashId() != 0;
    }

    public boolean isSet() {
        return hashedPassword != null;//this != SHARED_EMPTY
    }

/*    public HashingAlgorithm getHashing() {
        return hashing;
    }*/

    public static Password createRandom() {
        int length = 8 + AlixCommonUtils.random.nextInt(8);//min char length + 3 + (0-8) because 2^n results in a faster generation

        return fromUnhashed(new String(loopCharIterator.next(length)));
    }

/*    public static Password generatePseudoRandom(String generationBase) {
        byte hashId = 1;
        return new Password(ofHashId(hashId).hash(generationBase), hashId);
    }*/

    private static final Password SHARED_EMPTY = new Password(null, (byte) 0);
    private static final HashingAlgorithm CONFIG_HASH = Hashing.getConfigHashingAlgorithm();

    public static Password empty() {
        return SHARED_EMPTY;
    }

    public static Password fromUnhashed(String unhashedPassword) {
        String hashed = CONFIG_HASH.hash(unhashedPassword);
        return new Password(hashed, CONFIG_HASH);
    }

    public static Password readFromSaved(String savablePassword) {
        String[] s = savablePassword.split(":");
        String password = s[0];

        if (password.equals("null")) password = null;
        if (s.length == 1) return password == null ? empty() : fromUnhashed(password); //the old formatting support - we know it's unhashed since the hashing id was not saved

        return new Password(password, Byte.parseByte(s[1]));
    }

    static {
        char[] generationChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-+=".toCharArray();
        loopCharIterator = new LoopCharIterator(AlixCommonUtils.shuffle(generationChars));
    }
}