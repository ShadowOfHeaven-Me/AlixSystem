package alix.common.utils.other;

@FunctionalInterface
public interface BiIntFunction<T> {

    T apply(int a, int b);

}