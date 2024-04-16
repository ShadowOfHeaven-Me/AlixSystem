package alix.common.utils.collections.list;

import alix.common.utils.other.throwable.AlixException;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class LoopList<T> {

    private final T[] values;
    final int maxIndex;

    private LoopList(T[] values) {
        this.values = values;
        this.maxIndex = values.length - 1;
        if (maxIndex == -1) throw new AlixException("Cannot pass an empty array into a LoopList!");
    }

    abstract int nextIndex();

    abstract int previousIndex();

    abstract void setCurrentIndex0(int index);

    //public abstract int getCurrentIndex();

    public final void setCurrentIndex(int index) {
        if (index > values.length || index < 0)
            throw new AlixException("Index: " + index + " for size " + values.length);
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
        for (int i = 0; i < values.length; i++) if (v.equals(values[i])) return i;
        return -1;
    }

    public final T get(int i) {
        return this.values[i];
    }

    public final T[] getValues() {
        return values;
    }

    public final int size() {
        return values.length;
    }

    private static final class NormalLoopList<T> extends LoopList<T> {

        private int currentIndex;

        private NormalLoopList(T[] values) {
            super(values);
        }

        @Override
        int nextIndex() {
            return currentIndex = nextLoopIndex(currentIndex, maxIndex);
        }

        @Override
        int previousIndex() {
            return currentIndex = previousLoopIndex(currentIndex, maxIndex);
        }

        @Override
        void setCurrentIndex0(int index) {
            this.currentIndex = index;
        }
    }

    private static final class ConcurrentLoopList<T> extends LoopList<T> {

        private final AtomicInteger currentIndex = new AtomicInteger();

        private ConcurrentLoopList(T[] values) {
            super(values);
        }

        @Override
        int nextIndex() {
            return nextLoopIndex(this.currentIndex, this.maxIndex);
        }

        @Override
        int previousIndex() {
            return previousLoopIndex(this.currentIndex, this.maxIndex);
        }

        @Override
        void setCurrentIndex0(int index) {
            this.currentIndex.set(index);
        }
    }

    public static int previousLoopIndex(int i, int maxIndex) {
        return i != 0 ? i - 1 : maxIndex;
    }

    public static int previousLoopIndex(AtomicInteger i, int maxIndex) {
        return i.get() != 0 ? i.decrementAndGet() : setAndGet(i, maxIndex);
    }

    public static int nextLoopIndex(int i, int maxIndex) {
        return i != maxIndex ? i + 1 : 0;
    }

    public static int nextLoopIndex(AtomicInteger i, int maxIndex) {
        return i.get() != maxIndex ? i.incrementAndGet() : setAndGet(i, 0);
    }

    private static int setAndGet(AtomicInteger o, int v) {
        o.set(v);
        return v;
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