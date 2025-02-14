package alix.common.login.premium;

import java.util.UUID;

public final class EncryptionData {

    private final String packetUsername;
    private final String serverUsername;
    private final byte[] token;
    private final ClientPublicKey publicKey;
    private final UUID uuid;

    public EncryptionData(String packetUsername, String serverUsername, byte[] token, ClientPublicKey publicKey, UUID uuid) {
        this.packetUsername = packetUsername;
        this.serverUsername = serverUsername;
        this.token = token;
        this.publicKey = publicKey;
        this.uuid = uuid;
    }

    public byte[] token() {
        return token;
    }

    public UUID premiumUUID() {
        return uuid;
    }

    public ClientPublicKey publicKey() {
        return publicKey;
    }

    public String packetUsername() {
        return packetUsername;
    }

    public String serverUsername() {
        return serverUsername;
    }
}