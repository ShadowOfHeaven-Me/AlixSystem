package alix.common.scheduler.impl.executor;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public final class DelegatingExecutorService extends AbstractExecutorService {

    private final Executor executor;

    private DelegatingExecutorService(Executor executor) {
        this.executor = executor;
    }

    public static DelegatingExecutorService of(Executor executor) {
        return new DelegatingExecutorService(executor);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.executor.execute(command);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public @NotNull List<Runnable> shutdownNow() {
        return List.of();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        return false;
    }
}