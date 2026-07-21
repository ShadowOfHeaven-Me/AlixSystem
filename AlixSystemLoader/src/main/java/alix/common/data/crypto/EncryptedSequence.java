package alix.common.data.crypto;

public final class EncryptedSequence {

    //unencrypted
    private final String decrypted;
    private final String encrypted;

    EncryptedSequence(String decrypted, String encrypted) {
        this.decrypted = decrypted;
        this.encrypted = encrypted;
    }

    public String decrypted() {
        return decrypted;
    }

    public String encrypted() {
        return this.encrypted;
    }

    public static EncryptedSequence fromEncrypted(String encrypted, String key) throws Exception {
        var dec = StringCrypto.decrypt(encrypted, key);
        return new EncryptedSequence(dec, encrypted);
    }

    public static EncryptedSequence fromUnencrypted(String dec, String key) throws Exception {
        var enc = StringCrypto.encrypt(dec, key);
        return new EncryptedSequence(dec, enc);
    }
}