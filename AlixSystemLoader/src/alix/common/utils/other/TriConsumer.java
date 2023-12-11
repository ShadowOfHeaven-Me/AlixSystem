package alix.common.utils.other;

@FunctionalInterface
public interface TriConsumer<K, T, V> {

    void accept(K K, T t, V v);

}