package alix.common.scheduler;

import alix.common.scheduler.impl.InterfaceAlixScheduler;
import alix.common.scheduler.runnables.AlixThread;
import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.scheduler.tasks.SchedulerTask;
import alix.common.utils.AlixCommonHandler;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class AlixScheduler {

    private static final InterfaceAlixScheduler impl = AlixCommonHandler.createSchedulerImpl();

    public static void sync(Runnable r) {
        impl.sync(r);
    }

    public static ExecutorService getSyncExecutor() {
        return impl.getSyncExecutor();
    }

    public static void async(Runnable r) {
        impl.async(r);
    }

    public static ExecutorService getAsyncExecutor() {
        return impl.getAsyncExecutor();
    }

    public static void asyncBlocking(Runnable r) {
        impl.asyncBlocking(r);
    }

    public static ExecutorService getAsyncBlockingExecutor() {
        return impl.getAsyncBlockingExecutor();
    }

    public static SchedulerTask runLaterSync(Runnable r, long d, TimeUnit u) {
        return impl.runLaterSync(r, d, u);
    }

    public static SchedulerTask runLaterAsync(Runnable r, long d, TimeUnit u) {
        return impl.runLaterAsync(r, d, u);
    }

    public static SchedulerTask repeatSync(Runnable r, long i, TimeUnit u) {
        return impl.repeatSync(r, i, u);
    }

    @CanIgnoreReturnValue
    public static SchedulerTask repeatAsync(Runnable r, long i, TimeUnit u) {
        return impl.repeatAsync(r, i, u);
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> r) {
        return impl.supplyAsync(r);
    }

    public static <T> AlixFuture<T> singleAlixFuture(Supplier<T> r) {
        return impl.singleAlixFuture(r);
    }

    public static void shutdown() {
        impl.shutdown();
    }

    @CanIgnoreReturnValue
    public static AlixThread newAlixThread(Runnable cmd, long millisDelay, String name) {
        return impl.newAlixThread(cmd, millisDelay, name);
    }

    private AlixScheduler() {
    }
}