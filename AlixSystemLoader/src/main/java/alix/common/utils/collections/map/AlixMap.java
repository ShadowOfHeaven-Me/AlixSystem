package alix.common.utils.collections.map;

import java.util.function.BiConsumer;

public interface AlixMap<K, V> extends Iterable<V> {

    /**
     * Adds the Value with its Key. Can disregards whether it was already present (implementation-dependent).
     * May lead to hash collisions if used without caution.
     **/
    void put(K key, V value);

    /**
     * Replaces the Value found by the Key. Returns: The previous, now replaced Value. Null if no Value was replaced.
     **/
    V replace(K key, V value);

    /**
     * Gets the Value associated with this Key.
     **/
    V get(K key);


    /**
     * Removes the entry by the given Key. Returns the last Value associated with the key (if any).
     **/
    V remove(K key);

    void forEach(BiConsumer<K, V> consumer);

}