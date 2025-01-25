package shadow.systems.login.autoin.premium;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.google.common.primitives.Longs;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * Encryption and decryption minecraft util for connection between servers
 * and paid Minecraft account clients.
 *
 * @author Games647 and FastLogin contributors
 */

//https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/protocol/EncryptionUtil.java

final class EncryptionUtil {

    static final int VERIFY_TOKEN_LENGTH = 4;
    static final String KEY_PAIR_ALGORITHM = "RSA";

    private static final int RSA_LENGTH = 1_024;

    private static final PublicKey MOJANG_SESSION_KEY;
    private static final int LINE_LENGTH = 76;
    private static final Base64.Encoder KEY_ENCODER = Base64.getMimeEncoder(
            LINE_LENGTH, "\n".getBytes(StandardCharsets.UTF_8)
    );
    private static final int MILLISECOND_SIZE = 8;
    private static final int UUID_SIZE = 2 * MILLISECOND_SIZE;

    static {
        try {
            MOJANG_SESSION_KEY = loadMojangSessionKey();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load Mojang session key", ex);
        }
    }

    private EncryptionUtil() {
        throw new RuntimeException("No instantiation of utility classes allowed");
    }

    /**
     * Generate an RSA key pair
     *
     * @return The RSA key pair.
     */
    static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);

            keyPairGenerator.initialize(RSA_LENGTH);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
            // Should be existing in every vm
            throw new ExceptionInInitializerError(nosuchalgorithmexception);
        }
    }

    /**
     * Generate a random token. This is used to verify that we are communicating with the same player
     * in a login session.
     *
     * @param random random generator
     * @return a token with 4 bytes long
     */
    static byte[] generateVerifyToken(Random random) {
        byte[] token = new byte[VERIFY_TOKEN_LENGTH];
        random.nextBytes(token);
        return token;
    }

    /**
     * Generate the server id based on client and server data.
     *
     * @param serverId     session for the current login attempt
     * @param sharedSecret shared secret between the client and the server
     * @param publicKey    public key of the server
     * @return the server id formatted as a hexadecimal string.
     */
    static String getServerIdHashString(String serverId, SecretKey sharedSecret, PublicKey publicKey) {
        byte[] serverHash = getServerIdHash(serverId, publicKey, sharedSecret);
        return (new BigInteger(serverHash)).toString(16);
    }

    /**
     * Decrypts the content and extracts the key spec.
     *
     * @param privateKey private server key
     * @param sharedKey  the encrypted shared key
     * @return shared secret key
     */
    static SecretKey decryptSharedKey(PrivateKey privateKey, byte[] sharedKey) throws Exception {
        return new SecretKeySpec(decrypt(privateKey, sharedKey), "AES");
    }

    static boolean verifyClientKey(ClientPublicKey clientKey, Instant verifyTimestamp, UUID premiumId) throws Exception {
        if (clientKey.expired(verifyTimestamp)) {
            return false;
        }

        Signature verifier = Signature.getInstance("SHA1withRSA");
        // key of the signer
        verifier.initVerify(MOJANG_SESSION_KEY);
        verifier.update(toSignable(clientKey, premiumId));
        return verifier.verify(clientKey.signature());
    }

    private static byte[] toSignable(ClientPublicKey clientPublicKey, UUID ownerPremiumId) {
        if (ownerPremiumId == null) {
            long expiry = clientPublicKey.expire().toEpochMilli();
            String encoded = KEY_ENCODER.encodeToString(clientPublicKey.key().getEncoded());
            return (expiry + "-----BEGIN RSA PUBLIC KEY-----\n" + encoded + "\n-----END RSA PUBLIC KEY-----\n")
                    .getBytes(StandardCharsets.US_ASCII);
        }

        byte[] keyData = clientPublicKey.key().getEncoded();
        return ByteBuffer.allocate(keyData.length + UUID_SIZE + MILLISECOND_SIZE)
                .putLong(ownerPremiumId.getMostSignificantBits())
                .putLong(ownerPremiumId.getLeastSignificantBits())
                .putLong(clientPublicKey.expire().toEpochMilli())
                .put(keyData)
                .array();
    }

    static boolean verifyNonce(byte[] expected, PrivateKey decryptionKey, byte[] encryptedNonce) throws Exception {
        byte[] decryptedNonce = decrypt(decryptionKey, encryptedNonce);
        return Arrays.equals(expected, decryptedNonce);
    }

    static boolean verifySignedNonce(byte[] nonce, PublicKey clientKey, long signatureSalt, byte[] signature)
            throws Exception {
        Signature verifier = Signature.getInstance("SHA256withRSA");
        // key of the signer
        verifier.initVerify(clientKey);

        verifier.update(nonce);
        verifier.update(Longs.toByteArray(signatureSalt));
        return verifier.verify(signature);
    }

    private static PublicKey loadMojangSessionKey()
            throws Exception {
        URL keyUrl = EncryptionUtil.class.getClassLoader().getResource("yggdrasil/yggdrasil_session_pubkey.der");
        byte[] keyData = Resources.toByteArray(keyUrl);
        KeySpec keySpec = new X509EncodedKeySpec(keyData);

        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    private static byte[] decrypt(PrivateKey key, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private static byte[] getServerIdHash(String sessionId, PublicKey publicKey, SecretKey sharedSecret) {
        @SuppressWarnings("deprecation")
        Hasher hasher = Hashing.sha1().newHasher();

        hasher.putBytes(sessionId.getBytes(StandardCharsets.ISO_8859_1));
        hasher.putBytes(sharedSecret.getEncoded());
        hasher.putBytes(publicKey.getEncoded());

        return hasher.hash().asBytes();
    }
}