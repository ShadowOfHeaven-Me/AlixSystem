package alix.common.scheduler.impl;

import alix.common.CommonAlixMain;
import alix.common.scheduler.tasks.SchedulerTask;
import alix.common.utils.AlixCommonUtils;

import java.util.concurrent.*;
import java.util.function.Supplier;

public abstract class AbstractAlixScheduler implements InterfaceAlixScheduler {

    private final ForkJoinPool forkJoinPool;
    protected final ScheduledThreadPoolExecutor poolExecutor;

    protected AbstractAlixScheduler() {
        int parallelisms = Math.max(Runtime.getRuntime().availableProcessors() * 4, 8);
        CommonAlixMain.logInfo("Allocated scheduler thread amount: " + parallelisms);
        this.forkJoinPool = new ForkJoinPool(parallelisms, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, e) -> AlixCommonUtils.logException(e), false);
        this.poolExecutor = new ScheduledThreadPoolExecutor(1);
        this.poolExecutor.setRemoveOnCancelPolicy(true);
    }

    @Override
    public void async(Runnable r) {
        this.forkJoinPool.execute(r);
    }

    @Override
    public <T> CompletableFuture<T> asyncFuture(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, this.forkJoinPool);
    }

    @Override
    public SchedulerTask runLaterAsync(Runnable r, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = this.poolExecutor.schedule(() -> async(r), delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public SchedulerTask repeatAsync(Runnable r, long interval, TimeUnit unit) {
        ScheduledFuture<?> future = this.poolExecutor.scheduleAtFixedRate(() -> async(r), interval, interval, unit);
        return () -> future.cancel(false);
    }

    @Override
    public void shutdown() {
        try {
            this.forkJoinPool.shutdown();
            this.forkJoinPool.awaitTermination(1, TimeUnit.MINUTES);
            this.poolExecutor.shutdown();
            this.poolExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

/*    private static final class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            AlixCommonUtils.logException(e);
        }
    }*/
}