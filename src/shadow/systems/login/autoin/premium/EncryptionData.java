package shadow.systems.login.autoin.premium;

import java.util.UUID;

final class EncryptionData {

    private final String username;
    private final byte[] token;
    private final ClientPublicKey publicKey;
    private final UUID uuid;

    public EncryptionData(String username, byte[] token, ClientPublicKey publicKey, UUID uuid) {
        this.username = username;
        this.token = token;
        this.publicKey = publicKey;
        this.uuid = uuid;
    }

    public byte[] token() {
        return token;
    }

    public UUID uuid() {
        return uuid;
    }

    public ClientPublicKey publicKey() {
        return publicKey;
    }

    public String username() {
        return username;
    }
}