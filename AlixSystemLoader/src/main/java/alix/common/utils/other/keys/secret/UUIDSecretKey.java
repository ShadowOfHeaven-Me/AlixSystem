package alix.common.utils.other.keys.secret;

import java.util.UUID;

final class UUIDSecretKey extends AbstractMapSecretKey<UUID> {

    private final UUID uuid;

    UUIDSecretKey(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID key() {
        return uuid;
    }

    @Override
    public String savableKey() {
        return this.uuid.toString();
    }
}