package alix.common.login.premium;

import com.github.retrooper.packetevents.util.crypto.SignatureData;

import java.security.PublicKey;
import java.time.Instant;

public final class ClientPublicKey {

    private final Instant expire;
    private final PublicKey key;
    private final byte[] signature;

    private ClientPublicKey(Instant expire, PublicKey key, byte[] signature) {
        this.expire = expire;
        this.key = key;
        this.signature = signature;
    }

    public Instant expire() {
        return expire;
    }

    public byte[] signature() {
        return signature;
    }

    public PublicKey key() {
        return key;
    }

    public boolean expired(Instant timestamp) {
        return !timestamp.isBefore(expire);
    }

    public SignatureData toSignatureData() {
        return new SignatureData(expire, key, signature);
    }

    public static ClientPublicKey createKey(SignatureData data) {
        if (data == null) return null;

        Instant expires = data.getTimestamp();
        PublicKey key = data.getPublicKey();
        byte[] signatureData = data.getSignature();

        return new ClientPublicKey(expires, key, signatureData);
    }
}