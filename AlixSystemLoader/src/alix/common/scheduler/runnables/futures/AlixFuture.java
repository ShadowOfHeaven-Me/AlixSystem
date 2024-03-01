package alix.common.scheduler.runnables.futures;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface AlixFuture<T> {

    void whenCompleted(Consumer<T> consumer);

    void whenCompletedAsync(Consumer<T> consumer);

    boolean hasCompleted();

    T value();

    static <T> AlixFuture<T> singleFuture(Executor e, Supplier<T> s) {
        return AbstractAlixFuture.singledFuture(e, s);
    }
}