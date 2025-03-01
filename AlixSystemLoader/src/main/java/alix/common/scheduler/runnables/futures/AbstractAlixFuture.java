package alix.common.scheduler.runnables.futures;

import alix.common.utils.AlixCommonUtils;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract class AbstractAlixFuture<T> implements AlixFuture<T>, Runnable {

    private final Supplier<T> supplier;

    private AbstractAlixFuture(Executor executor, Supplier<T> supplier) {
        this.supplier = supplier;
        executor.execute(this);
    }

    @Override
    public final void run() {
        try {
            this.postComplete(this.supplier.get());
        } catch (Throwable e) {
            AlixCommonUtils.logException(e);
        }
    }

    abstract void postComplete(T value);

    private static final class SingleFuture<T> extends AbstractAlixFuture<T> {

        private volatile Executor executor;
        private volatile Consumer<T> consumer;
        private volatile T value;

        private SingleFuture(Executor e, Supplier<T> s) {
            super(e, s);
        }

        @Override
        void postComplete(T value) {
            this.value = value;
            if (this.consumer != null) {
                if (executor != null) executor.execute(() -> this.consumer.accept(this.value));
                else this.consumer.accept(this.value);
            }
        }

        @Override
        public void unassign() {
            this.consumer = null;
            this.executor = null;
        }

        @Override
        public void whenCompleted(Consumer<T> consumer) {
            this.consumer = consumer;
            if (this.value != null) this.consumer.accept(this.value);
        }

        @Override
        public void whenCompleted(Consumer<T> consumer, Executor executor) {
            this.consumer = consumer;
            if (this.value != null) executor.execute(() -> this.consumer.accept(this.value));
            else this.executor = executor;
        }

        @Override
        public boolean hasCompleted() {
            return value != null;
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public boolean isCompletedFutureType() {
            return false;
        }
    }

    private static final class CompletedFuture<T> implements AlixFuture<T> {

        private final T value;

        private CompletedFuture(T value) {
            this.value = value;
        }

        @Override
        public void unassign() {//nothing can be assigned in this future impl
        }

        @Override
        public void whenCompleted(Consumer<T> consumer) {
            consumer.accept(this.value);
        }

        @Override
        public void whenCompleted(Consumer<T> consumer, Executor withExecutor) {
            withExecutor.execute(() -> consumer.accept(this.value));
        }

        @Override
        public boolean isCompletedFutureType() {
            return true;
        }

        @Override
        public boolean hasCompleted() {
            return true;
        }

        @Override
        public T value() {
            return this.value;
        }
    }

    //Creates a future, where #whenCompleted invocation overrides the current Consumer (and optionally Executor)
    static <T> AlixFuture<T> newSingleFuture(Executor e, Supplier<T> s) {
        return new SingleFuture<>(e, s);
    }

    static <T> AlixFuture<T> newCompletedFuture(T value) {
        return new CompletedFuture<>(value);
    }

/*    //Extension methods

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
    }*/
}