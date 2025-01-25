package alix.common.utils.other.keys.secret.reader;

import alix.common.utils.other.keys.secret.MapSecretKey;

import java.util.UUID;

final class UUIDKeyReaderImpl implements KeyReader<UUID> {

    @Override
    public MapSecretKey<UUID> readKey(String savableKey) {
        return MapSecretKey.uuidKey(UUID.fromString(savableKey));
    }
}