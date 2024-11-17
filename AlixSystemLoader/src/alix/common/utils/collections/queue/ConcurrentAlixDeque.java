package alix.common.utils.collections.queue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public final class ConcurrentAlixDeque<T> implements AlixQueue<T> {

    private final ConcurrentLinkedQueue<T> queue;

    public ConcurrentAlixDeque() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public T pollFirst() {
        return this.queue.poll();
    }

    @Override
    public void offerLast(T element) {
        this.queue.offer(element);
    }

    @Override
    public void clear() {
        this.queue.clear();
    }

    @Override
    public void forEach(Consumer<T> action) {
        this.queue.forEach(action);
    }
}