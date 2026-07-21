package alix.common.antibot.algorithms.any.types;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class DummyFuture implements ScheduledFuture<Object> {

    static final DummyFuture INSTANCE = new DummyFuture();

    private DummyFuture() {
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public Throwable cause() {
        return null;
    }

    @Override
    public Future<Object> addListener(GenericFutureListener<? extends Future<? super Object>> listener) {
        return null;
    }

    @Override
    public Future<Object> addListeners(GenericFutureListener<? extends Future<? super Object>>... listeners) {
        return null;
    }

    @Override
    public Future<Object> removeListener(GenericFutureListener<? extends Future<? super Object>> listener) {
        return null;
    }

    @Override
    public Future<Object> removeListeners(GenericFutureListener<? extends Future<? super Object>>... listeners) {
        return null;
    }

    @Override
    public Future<Object> sync() throws InterruptedException {
        return null;
    }

    @Override
    public Future<Object> syncUninterruptibly() {
        return null;
    }

    @Override
    public Future<Object> await() throws InterruptedException {
        return null;
    }

    @Override
    public Future<Object> awaitUninterruptibly() {
        return null;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return false;
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return false;
    }

    @Override
    public Object getNow() {
        return null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }
}