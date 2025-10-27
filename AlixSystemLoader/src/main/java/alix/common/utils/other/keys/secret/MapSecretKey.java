package alix.common.utils.other.keys.secret;

import ua.nanit.limbo.util.UUIDUtil;

import java.util.UUID;

public interface MapSecretKey<T> {

    T key();

    String savableKey();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    static MapSecretKey<UUID> fromName(String name) {
        return MapSecretKey.uuidKey(UUIDUtil.getOfflineModeUuid(name));
    }

    static MapSecretKey<UUID> uuidKey(UUID uuid) {
        return new UUIDSecretKey(uuid);
    }
}