package alix.common.utils.other.keys.secret;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public interface MapSecretKey<T> {

    T key();

    String savableKey();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    static MapSecretKey<UUID> fromName(String name) {
        return MapSecretKey.uuidKey(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)));
    }

    static MapSecretKey<UUID> uuidKey(UUID uuid) {
        return new UUIDSecretKey(uuid);
    }
}