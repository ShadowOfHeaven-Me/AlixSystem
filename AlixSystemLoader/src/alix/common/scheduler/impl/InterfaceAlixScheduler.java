package alix.common.scheduler.impl;

import alix.common.scheduler.runnables.AlixThread;
import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.scheduler.tasks.SchedulerTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface InterfaceAlixScheduler {

    void sync(Runnable r);

    void async(Runnable r);

    SchedulerTask runLaterSync(Runnable r, long d, TimeUnit u);

    SchedulerTask runLaterAsync(Runnable r, long d, TimeUnit u);

    SchedulerTask repeatSync(Runnable r, long d, TimeUnit u);

    SchedulerTask repeatAsync(Runnable r, long i, TimeUnit u);

    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);

    <T> AlixFuture<T> singleAlixFuture(Supplier<T> r);

    void shutdown();

    default AlixThread newAlixThread(Runnable cmd, long millisDelay, String name) {
        return new AlixThread(cmd, millisDelay, name);
    }
}