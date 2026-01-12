package alix.common.database;

@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> {

    void apply(T obj) throws E;

}