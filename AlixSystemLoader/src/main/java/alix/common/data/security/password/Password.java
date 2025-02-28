package alix.common.data.security.password;


import alix.common.data.security.password.hashing.Hashing;
import alix.common.data.security.password.hashing.HashingAlgorithm;
import alix.common.data.security.password.matcher.PasswordMatcher;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.collections.SecureRandomCharIterator;
import alix.common.utils.file.SaveUtils;
import alix.common.utils.other.throwable.AlixError;

import java.util.Arrays;

import static alix.common.data.security.password.matcher.PasswordMatchers.*;
import static alix.common.utils.AlixCommonUtils.generationChars;

public final class Password {

    public static final int MAX_PASSWORD_LEN = 30;
    private static final SecureRandomCharIterator RANDOM_CHAR_ITERATOR = new SecureRandomCharIterator(AlixCommonUtils.shuffle(generationChars));
    private final String hashedPassword;
    private final HashingAlgorithm hashing;
    private final String salt;
    private final PasswordMatcher matcher;
    //private final String savablePassword;

    private Password(String hashedPassword, byte hashId, String salt, byte matcherId) {
        this(hashedPassword, Hashing.ofHashId(hashId), salt, matcherOfId(matcherId));
    }

    private Password(String hashedPassword, byte hashId, String salt, PasswordMatcher matcher) {
        this(hashedPassword, Hashing.ofHashId(hashId), salt, matcher);
    }

    private Password(String hashedPassword, HashingAlgorithm hashing, String salt, PasswordMatcher matcher) {
        this.hashedPassword = hashedPassword;
        this.hashing = hashing;
        this.salt = salt;
        this.matcher = matcher;
    }

    public String toSavable() {
        //return hashedPassword + ":" + hashing.hashId() + ":" + salt;
        //if (!this.hasSalt()) return SaveUtils.asSavable(':', hashedPassword, hashing.hashId());
        return SaveUtils.asSavable(':', hashedPassword, hashing.hashId(), salt, matcher.matcherId());
    }

    public boolean isEqualTo(String unhashedPassword) {
        return this.hashedPassword != null && this.matcher.matches(this, unhashedPassword); //this.hashedPassword.equals(Hashing.hashSaltFirst(this.hashing, unhashedPassword, this.salt));
    }

/*    public boolean isCorrectLiteral(String hashedPassword) {
        return this.hashedPassword.equals(hashedPassword);
    }*/

    public byte getHashId() {
        return this.hashing.hashId();
    }

    public boolean isHashed() {
        return hashing.hashId() != 0;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public HashingAlgorithm getHashing() {
        return hashing;
    }

    public String getSalt() {
        return salt;
    }

    //false if the password was reset via /as rp <user>
    public boolean isSet() {
        return hashedPassword != null;//this != SHARED_EMPTY
    }

    public boolean hasSalt() {
        return !salt.isEmpty();
    }

/*    public HashingAlgorithm getHashing() {
        return hashing;
    }*/

    public static Password createRandom() {
        int length = 8 + AlixCommonUtils.random.nextInt(8);//min char length + 3 + (0-8) because 2^n results in a faster generation

        return fromUnhashed(new String(RANDOM_CHAR_ITERATOR.next(length)));
    }

/*    public static Password fromHashed(String hashedPassword, byte hashId) {
        return new Password(hashedPassword, hashId);
    }*/

/*    public static Password generatePseudoRandom(String generationBase) {
        byte hashId = 1;
        return new Password(ofHashId(hashId).hash(generationBase), hashId);
    }*/

    private static final Password SHARED_EMPTY = new Password(null, (byte) 0, "", ALIX_FORMAT);
    private static final HashingAlgorithm CONFIG_HASH = Hashing.getConfigHashingAlgorithm();

/*    public static void write(Password password, ByteArrayDataOutput output) {
        if (!password.isSet()) {
            output.writeByte(-1);
            return;
        }

        output.writeByte(password.getHashId());
        byte[] passBytes = password.getHashedPassword().getBytes(StandardCharsets.UTF_8);
        output.writeShort(passBytes.length);
        output.write(passBytes);
    }*/

    public static Password empty() {
        return SHARED_EMPTY;
    }

/*    public static Password read(ByteArrayDataInput input) {
        byte hashId = input.readByte();
        if (hashId == -1) return empty();
        short length = input.readShort();

        byte[] passBytes = new byte[length];
        input.readFully(passBytes);
        return fromHashed(new String(passBytes, StandardCharsets.UTF_8), hashId);
    }*/

    public static Password fromHashedMigrated(String hashedPassword, String salt, HashingAlgorithm algorithm) {
        return new Password(hashedPassword, algorithm, salt, MIGRATION_FORMAT);
    }

    public static Password fromUnhashed(String unhashedPassword) {
        if (!CONFIG_HASH.isHashing())
            return new Password(unhashedPassword, CONFIG_HASH, "", ALIX_FORMAT);

        String salt = Hashing.generateSalt();
        String hashed = Hashing.hashSaltFirst(CONFIG_HASH, unhashedPassword, salt);
        return new Password(hashed, CONFIG_HASH, salt, ALIX_FORMAT);
    }

    public static Password readFromSaved(String savablePassword) {
        String[] s = savablePassword.split(":");
        String password = s[0];

        if (password.equals("null")) return empty();

        switch (s.length) {
            case 1:
                return fromUnhashed(password); //the old formatting support - we know it's unhashed since the hashing id was not saved
            case 2:
                return new Password(password, Byte.parseByte(s[1]), "", ALIX_FORMAT);//support old, unsalted passwords read
            case 3:
                return new Password(password, Byte.parseByte(s[1]), s[2], ALIX_FORMAT);
            case 4:
                return new Password(password, Byte.parseByte(s[1]), s[2], Byte.parseByte(s[3]));
            default:
                throw new AlixError("Invalid savable password: '" + savablePassword + "' for savable length " + s.length + " with savable parts " + Arrays.toString(s) + "!");
        }
    }
}