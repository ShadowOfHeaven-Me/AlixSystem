package alix.common.utils.collections.queue.network;

import alix.common.utils.collections.queue.AlixDeque;
import alix.common.utils.collections.queue.AlixQueue;

import java.util.function.Consumer;

public final class AlixNetworkDeque<T> implements AlixQueue<T> {

    private final Object[] array;
    private int index;
    private AlixDeque<T> deque;

    public AlixNetworkDeque(int predictedAverageSize) {
        this.array = new Object[predictedAverageSize];
    }

    private boolean isArrayFull() {
        return this.index == this.array.length;
    }

    @Override
    public T pollFirst() {
        if (this.index == 0) return null;
        if (this.isArrayFull()) {
            if (!this.deque.isEmpty()) return this.deque.pollFirst();
            this.deque = null;
        }

        return (T) this.array[--this.index];
    }

    @Override
    public void offerLast(T element) {
        if (this.isArrayFull()) {
            if (this.deque == null) this.deque = new AlixDeque<>();
            this.deque.offerLast(element);
            return;
        }
        this.array[index++] = element;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.index; i++) this.array[i] = null;

        if (this.deque != null) this.deque.clear();
    }

    @Override
    public void forEach(Consumer<T> action) {
        for (int i = 0; i < this.index; i++) action.accept((T) this.array[i]);

        if (this.deque != null) this.deque.forEach(action);
    }
}