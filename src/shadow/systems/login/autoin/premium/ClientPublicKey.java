package shadow.systems.login.autoin.premium;

import alix.libs.com.github.retrooper.packetevents.util.crypto.SignatureData;

import java.security.PublicKey;
import java.time.Instant;

final class ClientPublicKey {

    private final Instant expire;
    private final PublicKey key;
    private final byte[] signature;

    ClientPublicKey(Instant expire, PublicKey key, byte[] signature) {
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

    boolean expired(Instant timestamp) {
        return !timestamp.isBefore(expire);
    }

    SignatureData toSignatureData() {
        return new SignatureData(expire, key, signature);
    }
}