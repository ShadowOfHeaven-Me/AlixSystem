package alix.common.utils.collections.list;

import alix.common.utils.other.throwable.AlixException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

public abstract class LoopList<T> {

    final Object[] values;
    final int maxIndex;

    private LoopList(Object[] values) {
        this.values = values;
        this.maxIndex = values.length - 1;
        if (maxIndex == -1) throw new AlixException("Cannot pass an empty array into a LoopList!");
    }

    private LoopList(int size) {
        this.values = new Object[size];
        this.maxIndex = values.length - 1;
        if (maxIndex == -1) throw new AlixException("Cannot pass an empty array into a LoopList!");
    }

    public abstract int nextIndex();

    abstract int previousIndex();

    abstract void setCurrentIndex0(int index);

    abstract int getAndSetCurrentIndex0(int index);

    public abstract int getCurrentIndex();

    public abstract int getCurrentAndIncrement();

    public abstract int getAndUpdate(IntUnaryOperator operator);

    public abstract int updateAndGet(IntUnaryOperator operator);

    abstract void set0(int index, Object value);

    abstract void lazySet(int index, Object value);

    abstract Object get0(int index);

    public final int getAndSetCurrentIndex(int index) {
        this.rangeCheck(index);
        return this.getAndSetCurrentIndex0(index);
    }

    public final void setCurrentIndex(int index) {
        this.rangeCheck(index);
        this.setCurrentIndex0(index);
    }

    private void rangeCheck(int index) {
        if (index >= values.length || index < 0)
            throw new AlixException("Index: " + index + " for size " + values.length);
    }

    public final void setNext(T value) {
        this.set0(this.nextIndex(), value);
    }

    public final void setPrevious(T value) {
        this.set0(this.previousIndex(), value);
    }

    public final void setValue(int i, T value) {
        this.set0(i, value);
    }

    public final T next() {
        return this.get(this.nextIndex());
    }

    public final T current() {
        return this.get(this.getCurrentIndex());
    }

    public final T previous() {
        return this.get(this.previousIndex());
    }

    public final void drain(int size, Consumer<T> consumer) {
        for (int i = 0; i < size; i++) {
            //AlixCommonMain.logError("AAAAAA " + i + " " + this.get(i) + " " + size);
            consumer.accept(this.get(i));
            this.lazySet(i, null);
        }
    }

    public final void clear() {
        for (int i = 0; i < this.values.length; i++) this.lazySet(i, null);
    }

    public final void clear(int size0) {
        for (int i = 0; i < size0; i++) this.lazySet(i, null);
    }

    public final boolean contains(T v) {
        for (Object t : values) if (v.equals(t)) return true;
        return false;
    }

    public final int indexOf(T v) {
        for (int i = 0; i < values.length; i++) if (v.equals(values[i])) return i;
        return -1;
    }

    public final T get(int i) {
        return (T) this.get0(i);
    }

    public final Object[] getValues() {
        return values;
    }

    public final int size() {
        return values.length;
    }

    private static final class NormalLoopList<T> extends LoopList<T> {

        private int currentIndex;

        private NormalLoopList(Object[] values) {
            super(values);
        }

        private NormalLoopList(int size) {
            super(size);
        }

        @Override
        public int nextIndex() {
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

        @Override
        int getAndSetCurrentIndex0(int index) {
            int old = this.currentIndex;
            this.currentIndex = index;
            return old;
        }

        @Override
        public int getCurrentIndex() {
            return currentIndex;
        }

        @Override
        public int getCurrentAndIncrement() {
            return this.currentIndex++;
        }

        @Override
        public int getAndUpdate(IntUnaryOperator operator) {
            int old = this.currentIndex;
            this.currentIndex = operator.applyAsInt(this.currentIndex);
            return old;
        }

        @Override
        public int updateAndGet(IntUnaryOperator operator) {
            return this.currentIndex = operator.applyAsInt(this.currentIndex);
        }

        @Override
        void set0(int index, Object value) {
            this.values[index] = value;
        }

        @Override
        void lazySet(int index, Object value) {
            this.values[index] = value;//same impl
        }

        @Override
        Object get0(int index) {
            return this.values[index];
        }
    }

    private static final class ConcurrentLoopList<T> extends LoopList<T> {

        //Ensure the array's elements thread visibility by using MethodHandles
        private static final MethodHandle setElement = MethodHandles.arrayElementSetter(Object[].class);
        private static final MethodHandle getElement = MethodHandles.arrayElementGetter(Object[].class);
        private final AtomicInteger currentIndex = new AtomicInteger();

        private ConcurrentLoopList(Object[] values) {
            super(values);
        }

        private ConcurrentLoopList(int size) {
            super(size);
        }

        @Override
        public int nextIndex() {
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

        @Override
        int getAndSetCurrentIndex0(int index) {
            return this.currentIndex.getAndSet(index);
        }

        @Override
        public int getCurrentIndex() {
            return this.currentIndex.get();
        }

        @Override
        public int getCurrentAndIncrement() {
            return this.currentIndex.getAndIncrement();
        }

        @Override
        public int getAndUpdate(IntUnaryOperator operator) {
            return this.currentIndex.getAndUpdate(operator);
        }

        @Override
        public int updateAndGet(IntUnaryOperator operator) {
            return this.currentIndex.updateAndGet(operator);
        }

        @Override
        void set0(int index, Object value) {
            try {
                setElement.invokeExact(this.values, index, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        void lazySet(int index, Object value) {
            this.values[index] = value;
        }

        @Override
        Object get0(int index) {
            try {
                return getElement.invokeExact(this.values, index);
            } catch (Throwable e) {
                throw new AlixException(e);
            }
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

    public static <T> LoopList<T> ofSize(int size) {
        return new NormalLoopList<>(size);
    }

    @SafeVarargs
    public static <T> LoopList<T> newConcurrent(T... array) {
        return new ConcurrentLoopList<>(array);
    }

    public static <T> LoopList<T> newConcurrentOfSize(int size) {
        return new ConcurrentLoopList<>(size);
    }
}