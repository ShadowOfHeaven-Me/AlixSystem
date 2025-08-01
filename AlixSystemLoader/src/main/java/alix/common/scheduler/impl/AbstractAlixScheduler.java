package alix.common.scheduler.impl;

import alix.common.AlixCommonMain;
import alix.common.scheduler.impl.executor.DelegatingExecutorService;
import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.scheduler.tasks.SchedulerTask;
import alix.common.utils.AlixCommonHandler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.throwable.AlixException;

import java.util.concurrent.*;
import java.util.function.Supplier;

public abstract class AbstractAlixScheduler implements InterfaceAlixScheduler {

    private final ExecutorService asyncExecutor;
    private final ExecutorService asyncBlockingExecutor;
    protected final ScheduledThreadPoolExecutor poolExecutor;

    private final ExecutorService syncExecutor;

    //For now disabled \/
    //TO DO: Fix errors not being logged with AlixCommonUtils.logException(Exception) when ThreadPoolExecutor is used
    protected AbstractAlixScheduler() {
        //ThreadFactory virtualThreadFactory = VirtualThreadUtil.virtualThreadFactory(ForkJoinPool.defaultForkJoinWorkerThreadFactory);
        int parallelisms = 2;//Math.max(Runtime.getRuntime().availableProcessors(), 2);//at least two threads
        AlixCommonMain.logInfo("Async scheduler parallelisms: " + parallelisms);
        //parallelisms == 1 ?//no need to create a ForkJoinPool for only one thread
        //new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()) ://the default returned with Executors.newSingleThreadExecutor, but without the unnecessary extra delegation
        this.asyncExecutor = new ForkJoinPool(parallelisms, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, e) -> AlixCommonUtils.logException(e), false);

        this.poolExecutor = new ScheduledThreadPoolExecutor(1);
        this.poolExecutor.setRemoveOnCancelPolicy(true);

        this.asyncBlockingExecutor = AlixCommonHandler.createExecutorForBlockingTasks();

        this.syncExecutor = DelegatingExecutorService.of(this::sync);
        //this.asyncExecutor = this.poolExecutor;
    }

    @Override
    public ExecutorService getSyncExecutor() {
        return this.syncExecutor;
    }

    @Override
    public void async(Runnable r) {
        this.asyncExecutor.execute(new ErrorReportingRunnable(r));
    }

    @Override
    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    @Override
    public void asyncBlocking(Runnable r) {
        this.asyncBlockingExecutor.execute(new ErrorReportingRunnable(r));
    }

    @Override
    public ExecutorService getAsyncBlockingExecutor() {
        return asyncBlockingExecutor;
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(new ErrorReportingSupplier<>(supplier), this.asyncExecutor);
    }

    @Override
    public <T> AlixFuture<T> singleAlixFuture(Supplier<T> r) {
        return AlixFuture.singleFuture(this.asyncExecutor, new ErrorReportingSupplier<>(r));
    }

    @Override
    public SchedulerTask runLaterAsync(Runnable r, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = this.poolExecutor.schedule(new ErrorReportingRunnable(r), delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public SchedulerTask repeatAsync(Runnable r, long interval, TimeUnit unit) {
        ScheduledFuture<?> future = this.poolExecutor.scheduleAtFixedRate(new ErrorReportingRunnable(r), interval, interval, unit);
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
            throw new AlixException(e);
        }
    }

    private static final class ErrorReportingRunnable implements Runnable {

        private final Runnable delegate;

        private ErrorReportingRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                this.delegate.run();
            } catch (Throwable e) {
                AlixCommonUtils.logException(e);
            }
        }
    }

    private static final class ErrorReportingSupplier<T> implements Supplier<T> {

        private final Supplier<T> delegate;

        private ErrorReportingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            try {
               return this.delegate.get();
            } catch (Throwable e) {
                AlixCommonUtils.logException(e);
            }
            return null;
        }
    }
/*    private static final class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            AlixCommonUtils.logException(e);
        }
    }*/
}