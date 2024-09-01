package alix.common.utils.collections.queue;

import alix.common.utils.other.annotation.ScheduledForFix;

@ScheduledForFix
public final class ConcurrentAlixDeque<T> extends AlixDeque<T> {

    //Any modifications are synchronized
    private final Object lock;

    public ConcurrentAlixDeque() {
        this(false);
    }

    public ConcurrentAlixDeque(boolean internalLock) {
        this.lock = internalLock ? new Object() : this;
    }

    public ConcurrentAlixDeque(Object lock) {
        this.lock = lock;
    }

    @Override
    public void addNodeLast(Node<T> node) {//any node addition synchronized
        synchronized (lock) {//only this addition method is synchronized, as all the other point towards this one
            super.addNodeLast(node);
        }
    }

    @Override
    public T pollFirst() {//any node removal synchronized
        synchronized (lock) {
            return super.pollFirst();
        }
    }

    @Override
    public void clear() {//any node clearing synchronized
        synchronized (lock) {
            super.clear();
        }
    }
}