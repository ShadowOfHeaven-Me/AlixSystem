package alix.common.database;

@FunctionalInterface
public interface ThrowableFunction<V, T, E extends Throwable> {

    V apply(T t) throws E;

}