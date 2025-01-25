package alix.common.utils;

import alix.common.utils.other.annotation.OptimizationCandidate;
import com.google.common.cache.CacheBuilder;

public final class AlixCache {

    //with caffeine
    @OptimizationCandidate
    public static <K, V> CacheBuilder<K, V> newBuilder() {
        return (CacheBuilder<K, V>) CacheBuilder.newBuilder();
    }
}