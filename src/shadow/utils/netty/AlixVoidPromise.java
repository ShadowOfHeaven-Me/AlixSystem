package shadow.utils.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class AlixVoidPromise implements ChannelPromise {

    private final ChannelPromise promise;

    public AlixVoidPromise(Channel channel) {
        this.promise = channel.newPromise();
    }

    public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        return promise.addListener(listener);
    }

    @SafeVarargs
    public final ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        return promise.addListeners(listeners);
    }

    public ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        return promise.removeListener(listener);
    }

    @SafeVarargs
    public final ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        return promise.removeListeners(listeners);
    }

    public ChannelPromise await() throws InterruptedException {
        return promise.await();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return promise.await(timeout, unit);
    }

    public boolean await(long timeoutMillis) throws InterruptedException {
        return promise.await(timeoutMillis);
    }

    public ChannelPromise awaitUninterruptibly() {
        return promise.awaitUninterruptibly();
    }

    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return promise.awaitUninterruptibly(timeout, unit);
    }

    public boolean awaitUninterruptibly(long timeoutMillis) {
        return promise.awaitUninterruptibly(timeoutMillis);
    }

    public Channel channel() {
        return promise.channel();
    }

    public boolean isDone() {
        return promise.isDone();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        return promise.get();
    }

    @Override
    public Void get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return promise.get(timeout, unit);
    }

    public boolean isSuccess() {
        return promise.isSuccess();
    }

    public boolean setUncancellable() {
        return promise.setUncancellable();
    }

    public boolean isCancellable() {
        return promise.isCancellable();
    }

    public boolean isCancelled() {
        return promise.isCancelled();
    }

    public Throwable cause() {
        return promise.cause();
    }

    public ChannelPromise sync() throws InterruptedException {
        return promise.sync();
    }

    public ChannelPromise syncUninterruptibly() {
        return promise.syncUninterruptibly();
    }

    public ChannelPromise setFailure(Throwable cause) {
        return promise.setFailure(cause);
    }

    public ChannelPromise setSuccess() {
        return promise.setSuccess();
    }

    public boolean tryFailure(Throwable cause) {
        return promise.tryFailure(cause);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return promise.cancel(mayInterruptIfRunning);
    }

    public boolean trySuccess() {
        return promise.trySuccess();
    }

    public ChannelPromise setSuccess(Void result) {
        return promise.setSuccess(result);
    }

    public boolean trySuccess(Void result) {
        return promise.trySuccess(result);
    }

    public Void getNow() {
        return promise.getNow();
    }

    public ChannelPromise unvoid() {
        return promise.unvoid();
    }

    public boolean isVoid() {
        return promise.isVoid();
    }
}