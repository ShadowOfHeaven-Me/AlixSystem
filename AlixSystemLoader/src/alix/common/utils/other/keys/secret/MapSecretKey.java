package alix.common.utils.other.keys.secret;

import java.util.UUID;

public interface MapSecretKey<T> {

    T key();

    String savableKey();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    static MapSecretKey<UUID> uuidKey(UUID uuid) {
        return new UUIDSecretKey(uuid);
    }
}