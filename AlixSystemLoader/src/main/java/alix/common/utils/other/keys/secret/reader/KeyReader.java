package alix.common.utils.other.keys.secret.reader;

import alix.common.utils.other.keys.secret.MapSecretKey;

import java.util.UUID;

public interface KeyReader<T> {

    MapSecretKey<T> readKey(String savableKey);

    static KeyReader<UUID> uuidReaderImpl() {
        return new UUIDKeyReaderImpl();
    }
}