package alix.common.scheduler.impl;

import alix.common.scheduler.runnables.AlixThread;
import alix.common.scheduler.tasks.SchedulerTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface InterfaceAlixScheduler {

    void sync(Runnable r);

    void async(Runnable r);

    <T> CompletableFuture<T> asyncFuture(Supplier<T> supplier);

    SchedulerTask runLaterSync(Runnable r, long d, TimeUnit u);

    SchedulerTask runLaterAsync(Runnable r, long d, TimeUnit u);

    SchedulerTask repeatSync(Runnable r, long d, TimeUnit u);

    SchedulerTask repeatAsync(Runnable r, long i, TimeUnit u);

    void shutdown();

    default AlixThread newAlixThread(Runnable cmd, long millisDelay, String name) {
        return new AlixThread(cmd, millisDelay, name);
    }

/*    AlixScheduler instance = JavaHandler.createSchedulerImpl();

    static AlixScheduler get() {
        return instance;
    }*/
}