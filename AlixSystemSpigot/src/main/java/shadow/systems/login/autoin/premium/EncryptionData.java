package shadow.systems.login.autoin.premium;

import java.util.UUID;

final class EncryptionData {

    private final String username;
    private final byte[] token;
    private final ClientPublicKey publicKey;
    private final UUID uuid;

    EncryptionData(String username, byte[] token, ClientPublicKey publicKey, UUID uuid) {
        this.username = username;
        this.token = token;
        this.publicKey = publicKey;
        this.uuid = uuid;
    }

    byte[] token() {
        return token;
    }

    UUID uuid() {
        return uuid;
    }

    ClientPublicKey publicKey() {
        return publicKey;
    }

    String username() {
        return username;
    }
}