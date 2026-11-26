package alix.common.utils;

import com.github.benmanes.caffeine.cache.Caffeine;

public final class AlixCache {

    public static <K, V> Caffeine<K, V> newBuilder() {
        return (Caffeine<K, V>) Caffeine.newBuilder();
    }
}