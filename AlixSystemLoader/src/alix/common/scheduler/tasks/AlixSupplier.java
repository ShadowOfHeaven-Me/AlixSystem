package alix.common.scheduler.tasks;

import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public final class AlixSupplier<T> implements Supplier<T>, Runnable {

    private ReentrantLock lock;
    private Condition condition;
    private final Supplier<T> supplier;
    private T result;
    private volatile boolean completed;

    public AlixSupplier(Supplier<T> supplier, Executor executor) {
        this.supplier = supplier;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();

        this.lock.lock();
        executor.execute(this);
    }

    @Override
    public T get() {
        if (!completed) {
            try {
                this.condition.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    public void run() {
        this.result = this.supplier.get();
        this.completed = true;
        this.lock.unlock();
        this.condition.signalAll();
    }
}