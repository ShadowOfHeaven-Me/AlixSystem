package alix.common.utils.other;

@FunctionalInterface
public interface ByteArrayFunction<V> {

    byte[] getBytes(V v);

}