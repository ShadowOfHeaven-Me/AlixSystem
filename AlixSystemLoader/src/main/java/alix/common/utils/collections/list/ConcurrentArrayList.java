package alix.common.utils.collections.list;

import alix.common.utils.other.throwable.AlixException;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class ConcurrentArrayList<T> {

    private static final AtomicIntegerFieldUpdater<ConcurrentArrayList> SIZE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(ConcurrentArrayList.class, "size");
    //private static final AtomicReferenceFieldUpdater<ConcurrentArrayList, Boolean> GROWING_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ConcurrentArrayList.class, Boolean.class, "isGrowing");
    private volatile Object[] data;
    private volatile int size;
    private final ReentrantLock lock;
    private final Condition condition;

    public ConcurrentArrayList(int initialSize) {
        this.data = new Object[initialSize];
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    public void add(T t) {
        this.growToHoldExtra(1);
        int index = SIZE_UPDATER.getAndAdd(this, 1);
        AtomicArrayUtil.setElement(this.data, index, t);
    }

    private void growToHoldExtra(int extra) {
        //if (this.lock.isLocked()) this.await();
        if (this.size + extra > data.length) {
            this.lock.lock();
            try {
                if (this.size + extra > data.length)//recheck
                    this.grow(extra);
            } finally {
                this.lock.unlock();
            }
        }
    }

    private void grow(int by) {
        Object[] a = new Object[this.data.length + by];

    }

    private void await() {
        try {
            this.condition.await();
        } catch (InterruptedException e) {
            throw new AlixException(e);
        }
    }
}