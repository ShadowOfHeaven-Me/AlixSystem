package alix.common.utils.collections.list;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class LoopList<T> {

    private final T[] values;
    final int maxIndex;

    LoopList(T[] values) {
        this.values = values;
        int l = values.length;
        this.maxIndex = l;
    }

    public final void setNext(T value) {
        this.values[nextIndex()] = value;
    }

    public final void setValue(int i, T value) {
        values[i] = value;
    }

    public final T next() {
        return this.values[nextIndex()];
    }

    abstract int nextIndex();

    public abstract int getCurrentIndex();

    public final boolean contains(T v) {
        for (T t : values) if (v.equals(t)) return true;
        return false;
    }

    public final T get(short i) {
        return this.values[i];
    }

    public final T[] getValues() {
        return values;
    }

    public final int size() {
        return maxIndex;
    }

    private static final class NormalLoopList<T> extends LoopList<T> {

        private short currentIndex;

        private NormalLoopList(T[] values) {
            super(values);
        }

        int nextIndex() {
            return currentIndex != maxIndex ? currentIndex++ : (currentIndex = 0);
        }

        @Override
        public int getCurrentIndex() {
            return currentIndex;
        }
    }

    private static final class ConcurrentLoopList<T> extends LoopList<T> {

        private final AtomicInteger currentIndex = new AtomicInteger();

        private ConcurrentLoopList(T[] values) {
            super(values);
        }

        int nextIndex() {
            return currentIndex.get() != maxIndex ? currentIndex.getAndIncrement() : this.reset0();
        }

        private int reset0() {
            this.currentIndex.set(0);
            return 0;
        }

        @Override
        public int getCurrentIndex() {
            return currentIndex.get();
        }
    }

    public static <T> LoopList<T> of(T[] array) {
        return new NormalLoopList<>(array);
    }

    public static <T> LoopList<T> newConcurrent(T[] array) {
        return new ConcurrentLoopList<>(array);
    }
}