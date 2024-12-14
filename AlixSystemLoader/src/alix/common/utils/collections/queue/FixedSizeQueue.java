package alix.common.utils.collections.queue;

import alix.common.utils.other.throwable.AlixException;

import java.util.Arrays;
import java.util.function.Consumer;

public final class FixedSizeQueue<T> implements AlixQueue<T> {

    private final Object[] data;
    private int writerIndex;

    public FixedSizeQueue(int size) {
        this.data = new Object[size];
    }

    @Override
    public T pollFirst() {
        throw new AlixException(new UnsupportedOperationException("pollFirst()"));
    }

    public boolean contains(T e) {
        for (int i = 0; i < this.writerIndex; i++) {
            if (e.equals(data[i])) return true;
        }
        return false;
    }

    public boolean containsIdentity(T e) {
        for (int i = 0; i < this.writerIndex; i++) {
            if (e == data[i]) return true;
        }
        return false;
    }

    public int size() {
        return this.writerIndex;
    }

    @Override
    public void offerLast(T element) {
        this.data[this.writerIndex++] = element;
    }

    @Override
    public void clear() {
        Arrays.fill(this.data, 0, this.writerIndex, null);
        this.writerIndex = 0;
    }

    @Override
    public void forEach(Consumer<T> action) {
        for (int i = 0; i < this.writerIndex; i++) {
            action.accept((T) data[i]);
        }
    }
}