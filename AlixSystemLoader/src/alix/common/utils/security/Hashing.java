package alix.common.utils.security;

import alix.common.utils.config.ConfigProvider;
import alix.common.utils.security.types.Sha256;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashing {

    public static final byte CONFIG_HASH_ID;
    private static final HashingAlgorithm hash0, hash1, hash2, hash3, configHash;//Maybe an array of these hashes types in the future?

    public static HashingAlgorithm ofHashId(byte hashId) {
        switch (hashId) {
            case 0:
                return hash0;
            case 1:
                return hash1;
            case 2:
                return hash2;
            case 3:
                return hash3;
            default:
                throw new Error("Invalid hashId: " + hashId + " with the max being: 3");
        }
    }

    public static HashingAlgorithm getConfigHashingAlgorithm() {
        return configHash;
    }

    private static final class Hash0 implements HashingAlgorithm {

        @Override
        public final String hash(String s) {
            return s;//no hash
        }
    }

    private static final class Hash1 implements HashingAlgorithm {

        @Override
        public final String hash(String s) {//pretty fast, but possibly repeatable
            int hashCode = 0;
            char[] a = s.toCharArray();

            for (char c : a) hashCode = 31 * hashCode + c;//(hashCode << 5) - hashCode + c cannot be used since it could result in a little bit different generation

            return Integer.toString(hashCode);
        }
    }

    private static final class Hash2 implements HashingAlgorithm {

        private final MessageDigest md5;

        public Hash2() {
            //only get the hashing instance in a cracked server, no need for getting the algorithm in a premium server
            try {
                this.md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new InternalError("Unable to find the MD5 hashing algorithm.", e);// Please change the 'password-hash-type' parameter in the config,yml file!", e);
            }
        }

        @Override
        public final String hash(String s) {//md5 encryption into an uuid
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            byte[] hashedBytes = md5.digest(bytes);

            return uuidBitHash(hashedBytes);
        }
    }

    private static final class Hash3 implements HashingAlgorithm {

        private final Sha256 sha256 = new Sha256();

        @Override
        public final String hash(String s) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            byte[] hashedBytes = sha256.hash(bytes);

            return uuidBitHash(hashedBytes);
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

        return String.valueOf(mostSigBits + leastSigBits + hashCodeIn64Bits);
    }

    static {
        int i = ConfigProvider.config.getInt("password-hash-type");
        byte b = (byte) i;

        if (b != i || b < 0 || b > 3) b = 2;//Default

        CONFIG_HASH_ID = b; //Set from the config

        /*if (false) {//only get the hashing instance in a cracked server, no need for getting the algorithms to generate in a premium server
            hash0 = hash1 = hash2 = hash3 = defaultHash = null;
        } else {*/
        hash0 = new Hash0();
        hash1 = new Hash1();
        hash2 = new Hash2();
        hash3 = new Hash3();

        configHash = ofHashId(CONFIG_HASH_ID);
        //}
    }

    public static void init() {
    }

    private Hashing() {
    }
}