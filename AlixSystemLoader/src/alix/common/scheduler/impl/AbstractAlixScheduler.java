package alix.common.scheduler.impl;

import alix.common.AlixCommonMain;
import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.scheduler.tasks.SchedulerTask;
import alix.common.utils.AlixCommonUtils;

import java.util.concurrent.*;
import java.util.function.Supplier;

public abstract class AbstractAlixScheduler implements InterfaceAlixScheduler {

    private final ExecutorService asyncExecutor;
    protected final ScheduledThreadPoolExecutor poolExecutor;

    //For now disabled \/
    //TO DO: Fix errors not being logged with AlixCommonUtils.logException(Exception) when ThreadPoolExecutor is used
    protected AbstractAlixScheduler() {
        int parallelisms = Math.max(Runtime.getRuntime().availableProcessors() + 1, 2);//at least two threads
        AlixCommonMain.logInfo("Async scheduler parallelisms: " + parallelisms);
         //parallelisms == 1 ?//no need to create a ForkJoinPool for only one thread
                //new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()) ://the default returned with Executors.newSingleThreadExecutor, but without the unnecessary extra delegation
        this.asyncExecutor = new ForkJoinPool(parallelisms, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, e) -> AlixCommonUtils.logException(e), false);
        this.poolExecutor = new ScheduledThreadPoolExecutor(1);
        this.poolExecutor.setRemoveOnCancelPolicy(true);
    }

    @Override
    public void async(Runnable r) {
        this.asyncExecutor.execute(r);
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, this.asyncExecutor);
    }

    @Override
    public <T> AlixFuture<T> singleAlixFuture(Supplier<T> r) {
        return AlixFuture.singleFuture(this.asyncExecutor, r);
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
            this.asyncExecutor.shutdown();
            this.asyncExecutor.awaitTermination(1, TimeUnit.MINUTES);
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