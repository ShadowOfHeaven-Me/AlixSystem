package alix.common.scheduler.runnables.futures;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface AlixFuture<T> {

    void whenCompleted(Consumer<T> consumer);

    void whenCompleted(Consumer<T> consumer, Executor withExecutor);

    boolean hasCompleted();

    //Returns the current value for the future, null if not completed yet
    T value();

    static <T> AlixFuture<T> singleFuture(Executor e, Supplier<T> s) {
        return AbstractAlixFuture.newSingleFuture(e, s);
    }
}