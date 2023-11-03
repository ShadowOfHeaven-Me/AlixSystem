package alix.common.scheduler.impl;

import alix.common.scheduler.runnables.AlixThread;
import alix.common.scheduler.tasks.SchedulerTask;
import alix.common.utils.AlixCommonHandler;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AlixScheduler {

    private static final InterfaceAlixScheduler scheduler = AlixCommonHandler.createSchedulerImpl();

    public static void sync(Runnable r) {
        scheduler.sync(r);
    }

    public static void async(Runnable r) {
        scheduler.async(r);
    }

    public static <T> CompletableFuture<T> asyncFuture(Supplier<T> r) {
        return scheduler.asyncFuture(r);
    }

    public static SchedulerTask runLaterSync(Runnable r, long d, TimeUnit u) {
        return scheduler.runLaterSync(r, d, u);
    }

    public static SchedulerTask runLaterAsync(Runnable r, long d, TimeUnit u) {
        return scheduler.runLaterAsync(r, d, u);
    }

    public static SchedulerTask repeatSync(Runnable r, long i, TimeUnit u) {
        return scheduler.repeatSync(r, i, u);
    }

    public static SchedulerTask repeatAsync(Runnable r, long i, TimeUnit u) {
        return scheduler.repeatAsync(r, i, u);
    }

    public static void shutdown() {
        scheduler.shutdown();
    }

    @CanIgnoreReturnValue
    public static AlixThread newAlixThread(Runnable cmd, long millisDelay, String name) {
        return scheduler.newAlixThread(cmd, millisDelay, name);
    }
}