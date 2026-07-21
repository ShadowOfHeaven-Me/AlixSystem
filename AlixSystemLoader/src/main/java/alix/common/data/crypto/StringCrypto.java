package alix.common.data.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public final class StringCrypto {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;      // recommended for GCM
    private static final int KEY_LEN = 256;
    private static final int PBKDF2_ITERS = 120_000;

    public static String encrypt(String s, String e) throws GeneralSecurityException {
        byte[] salt = randomBytes(SALT_LEN);
        SecretKey key = deriveKey(e, salt);

        byte[] iv = randomBytes(IV_LEN);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] ciphertext = cipher.doFinal(s.getBytes(StandardCharsets.UTF_8));

        ByteBuffer out = ByteBuffer.allocate(4 + salt.length + 4 + iv.length + ciphertext.length);
        out.putInt(salt.length).put(salt);
        out.putInt(iv.length).put(iv);
        out.put(ciphertext);

        return Base64.getEncoder().encodeToString(out.array());
    }

    public static String decrypt(String encoded, String e) throws GeneralSecurityException {
        byte[] data = Base64.getDecoder().decode(encoded);
        ByteBuffer in = ByteBuffer.wrap(data);

        int saltLen = in.getInt();
        byte[] salt = new byte[saltLen];
        in.get(salt);

        int ivLen = in.getInt();
        byte[] iv = new byte[ivLen];
        in.get(iv);

        byte[] ciphertext = new byte[in.remaining()];
        in.get(ciphertext);

        SecretKey key = deriveKey(e, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] plain = cipher.doFinal(ciphertext);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static SecretKey deriveKey(String e, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(e.toCharArray(), salt, PBKDF2_ITERS, KEY_LEN);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        RNG.nextBytes(b);
        return b;
    }
}