package alix.common.scheduler.runnables.futures;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract class AbstractAlixFuture<T> extends ForkJoinTask<Void> implements AlixFuture<T>, Runnable {

    private final Supplier<T> supplier;

    private AbstractAlixFuture(Executor executor, Supplier<T> supplier) {
        this.supplier = supplier;
        executor.execute(this);
    }

    @Override
    public final void run() {
        this.postComplete(this.supplier.get());
    }

    abstract void postComplete(T value);

    private static final class SingleFuture<T> extends AbstractAlixFuture<T> {

        private final Executor executor;
        private volatile Consumer<T> consumer;
        private volatile T value;

        private SingleFuture(Executor e, Supplier<T> s) {
            super(e, s);
            this.executor = e;
        }

        @Override
        void postComplete(@NotNull T value) {
            this.value = value;
            if (this.consumer != null) this.consumer.accept(value);
        }

        @Override
        public void whenCompleted(Consumer<T> consumer) {
            this.consumer = consumer;
            if (this.value != null) this.consumer.accept(this.value);
        }

        @Override
        public void whenCompletedAsync(Consumer<T> consumer) {
            this.consumer = consumer;
            if (this.value != null) this.executor.execute(() -> this.consumer.accept(this.value));
        }

        @Override
        public boolean hasCompleted() {
            return value != null;
        }

        @Override
        public T value() {
            return value;
        }
    }

    //Creates a future, where the #whenCompleted methods override the current consumer
    static <T> AlixFuture<T> singledFuture(Executor e, Supplier<T> s) {
        return new SingleFuture<>(e, s);
    }

    //Extension methods

    @Override
    public final Void getRawResult() {
        return null;
    }

    @Override
    protected final void setRawResult(Void value) {
    }

    @Override
    protected final boolean exec() {
        this.run();
        return false;
    }
}