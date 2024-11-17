package alix.common.data.security;

import alix.common.data.security.types.Sha256;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.other.throwable.AlixException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class Hashing {

    public static final byte CONFIG_HASH_ID;
    private static final HashingAlgorithm[] hashingAlgorithms;
    private static final HashingAlgorithm configHash;
    private static final SecureRandom SECURE_RANDOM;

    public static String hash(HashingAlgorithm algorithm, String unhashedPassword, String salt) {
        return algorithm.hash((salt != null ? salt : "") + unhashedPassword);
    }

    public static HashingAlgorithm ofHashId(byte hashId) {
        return hashingAlgorithms[hashId];//will throw the error if the index is messed up regardless
        /*if (hashId < hashingAlgoritms.length && hashId >= 0) return hashingAlgoritms[hashId];
        throw new AlixError("Invalid hashId: " + hashId + " with the max being: " + hashingAlgoritms.length);*/
    }

    public static HashingAlgorithm getConfigHashingAlgorithm() {
        return configHash;
    }

    public static String generateSalt() {
        return Long.toHexString(SECURE_RANDOM.nextLong());
    }

    private static final class Hash0 implements HashingAlgorithm {

        @Override
        public String hash(String s) {
            return s;//no hash
        }

        @Override
        public byte hashId() {
            return 0;
        }
    }

    private static final class Hash1 implements HashingAlgorithm {

        @Override
        public String hash(String s) {//pretty fast, but possibly repeatable
            int hashCode = 0;
            char[] a = s.toCharArray();

            for (char c : a)
                hashCode = 31 * hashCode + c;//(hashCode << 5) - hashCode + c cannot be used since it could result in a little bit different generation

            return Integer.toString(hashCode);
        }

        @Override
        public byte hashId() {
            return 1;
        }
    }

    private static final class Hash2 implements HashingAlgorithm {

        private final MessageDigest md5 = getDigest("MD5");

        @Override
        public String hash(String s) {//md5 encryption into an uuid
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            byte[] hashedBytes = md5.digest(bytes);

            return uuidBitHash(hashedBytes);
        }

        @Override
        public byte hashId() {
            return 2;
        }
    }

    private static final class Hash3 implements HashingAlgorithm {

        private final Sha256 sha256 = Sha256.INSTANCE;

        @Override
        public String hash(String s) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            byte[] hashedBytes = sha256.hash(bytes);

            return uuidBitHash(hashedBytes);
        }

        @Override
        public byte hashId() {
            return 3;
        }
    }

/*    private static final class Hash4 implements HashingAlgorithm {

        @Override
        public String hash(String s) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public byte hashId() {
            return 4;
        }
    }*/

    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new AlixException(e, "The algorithm " + algorithm + " is not available on your server instance!");// Please change the parameter 'password-hash-type' in your config.yml file to 1");
        }
    }

/*    private static final class Hash4 implements HashingAlgorithm {

        # 4 - Uses bit shifting, very fast and unlikely to repeat, stored in a 64-bit integer

        @Override
        public final String hash(String s) {
            char[] a = s.toCharArray();
            long hash = a.length;
            for (int i = 0; i < a.length; i++) {
                char c = a[i];
                hash += (hash + c << 7) << (i + i & 1) + c >> 1;//just some random stuff I came up with
            }
            return Long.toString(hash);
        }
    }*/

    private static String uuidBitHash(byte[] hashedBytes) {
        //this is what UUID also does, but for the purpose of randomization and optimization I won't do that
        /*  md5Bytes[6]  &= 0x0f;  *//* clear version        *//*
            md5Bytes[6]  |= 0x30;  *//* set to version 3     *//*
            md5Bytes[8]  &= 0x3f;  *//* clear variant        *//*
            md5Bytes[8]  |= 0x80;  *//* set to IETF variant  */

        long mostSigBits = 0;
        long leastSigBits = 0;

        assert hashedBytes.length == 16 : "Hashed bytes length is different from 16 - " + hashedBytes.length;

        for (byte i = 0; i < 8; i++)
            mostSigBits = (mostSigBits << 8) | (hashedBytes[i] & 0xff);
        for (byte i = 8; i < 16; i++)
            leastSigBits = (leastSigBits << 8) | (hashedBytes[i] & 0xff);

        long hilo = mostSigBits ^ leastSigBits;
        long hashCodeIn64Bits = (hilo >> 32) ^ hilo;

        /*
        long hilo = mostSigBits ^ leastSigBits; UUID's hash code generation
        return ((int)(hilo >> 32)) ^ (int) hilo
        */

        return Long.toString(mostSigBits + leastSigBits + hashCodeIn64Bits);
    }

    static {
        int i = ConfigProvider.config.getInt("password-hash-type");
        byte b = (byte) i;

        if (b != i || b < 0 || b > 3) b = 3;//Default

        CONFIG_HASH_ID = b; //Set from the config
        hashingAlgorithms = new HashingAlgorithm[]{new Hash0(), new Hash1(), new Hash2(), new Hash3()};
        configHash = ofHashId(CONFIG_HASH_ID);
        SECURE_RANDOM = new SecureRandom();
    }

    public static void init() {
    }

    private Hashing() {
    }
}