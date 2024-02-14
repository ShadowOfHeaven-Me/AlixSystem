package alix.common.utils.collections.list;

import alix.common.utils.other.throwable.AlixException;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class LoopList<T> {

    private final T[] values;
    final int size;
    final int maxIndex;

    private LoopList(T[] values) {
        this.values = values;
        this.size = values.length;
        this.maxIndex = size - 1;
    }

    abstract int nextIndex();

    abstract int previousIndex();

    abstract void setCurrentIndex0(int index);

    //public abstract int getCurrentIndex();

    public final void setCurrentIndex(int index) {
        if (index > size || index < 0) throw new AlixException("Index: " + index + " for size " + size);
        this.setCurrentIndex0(index);
    }

    public final void setNext(T value) {
        this.values[this.nextIndex()] = value;
    }

    public final void setPrevious(T value) {
        this.values[this.previousIndex()] = value;
    }

    public final void setValue(int i, T value) {
        values[i] = value;
    }

    public final T next() {
        return this.values[this.nextIndex()];
    }

/*    public final T current() {
        return this.values[this.getCurrentIndex()];
    }*/

    public final T previous() {
        return this.values[this.previousIndex()];
    }

    public final boolean contains(T v) {
        for (T t : values) if (v.equals(t)) return true;
        return false;
    }

    public final int indexOf(T v) {
        for (int i = 0; i < size; i++) if (v.equals(values[i])) return i;
        return -1;
    }

    public final T get(int i) {
        return this.values[i];
    }

    public final T[] getValues() {
        return values;
    }

    public final int size() {
        return size;
    }

    private static final class NormalLoopList<T> extends LoopList<T> {

        private int currentIndex;

        private NormalLoopList(T[] values) {
            super(values);
        }

        @Override
        int nextIndex() {
            return currentIndex != maxIndex ? ++currentIndex : (currentIndex = 0);
        }

        @Override
        int previousIndex() {
            return currentIndex != 0 ? --currentIndex : (currentIndex = maxIndex);
        }

        @Override
        void setCurrentIndex0(int index) {
            this.currentIndex = index;
        }

/*        @Override
        public int getCurrentIndex() {
            return currentIndex;
        }*/
    }

    private static final class ConcurrentLoopList<T> extends LoopList<T> {

        private final AtomicInteger currentIndex = new AtomicInteger();

        private ConcurrentLoopList(T[] values) {
            super(values);
        }

        @Override
        int nextIndex() {
            return currentIndex.get() != maxIndex ? currentIndex.getAndIncrement() : setAndGet(0);
        }

        @Override
        int previousIndex() {
            return currentIndex.get() != 0 ? currentIndex.decrementAndGet() : setAndGet(maxIndex);
        }

        @Override
        void setCurrentIndex0(int index) {
            this.currentIndex.set(index);
        }

        private int setAndGet(int i) {
            this.currentIndex.set(i);
            return i;
        }

/*        @Override
        public int getCurrentIndex() {
            return currentIndex.get();
        }*/
    }

    @SafeVarargs
    public static <T> LoopList<T> of(T... array) {
        return new NormalLoopList<>(array);
    }

    @SafeVarargs
    public static <T> LoopList<T> newConcurrent(T... array) {
        return new ConcurrentLoopList<>(array);
    }
}