package alix.common.utils.collections.map;

import java.util.function.BiConsumer;

public interface AlixMap<K, V> extends Iterable<V> {

    void put(K key, V value);

    V get(K key);

    V remove(K key);

    void forEach(BiConsumer<K, V> consumer);

}