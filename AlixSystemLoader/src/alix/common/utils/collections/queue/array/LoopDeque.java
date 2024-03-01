package alix.common.utils.collections.queue.array;

import alix.common.AlixCommonMain;
import alix.common.utils.other.throwable.AlixException;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class LoopDeque<T> {

    private final Object[] array;

    private LoopDeque(int fixedSize) {
        this.array = new Object[fixedSize];
        if (fixedSize <= 0)
            throw new AlixException("Fixed pointer deque size must be greater than zero! Got: " + fixedSize);
        if (fixedSize == 1)
            AlixCommonMain.logWarning("For the size 1 in " + this.getClass().getSuperclass().getSimpleName() + " the class is unnecessary!");
    }

    public final T getAndReplaceWith(T replacement) {
        T value = (T) this.array[this.nextGetterIndex()];
        this.array[this.nextSetterIndex()] = replacement;
        return value;
    }

    public final void offerNext(T t) {
        this.array[this.nextSetterIndex()] = t;
    }

    abstract int nextGetterIndex();

    abstract int nextSetterIndex();

    private static final class ConcurrentLoopDeque<T> extends LoopDeque<T> {

        private final AtomicInteger getterIndex, setterIndex;
        private final int maxIndex;

        private ConcurrentLoopDeque(int fixedSize) {
            super(fixedSize);
            this.getterIndex = new AtomicInteger();
            this.setterIndex = new AtomicInteger();
            this.maxIndex = fixedSize - 1;
        }

        @Override
        int nextGetterIndex() {
            return getterIndex.get() != maxIndex ? getterIndex.getAndIncrement() : setAndGet(getterIndex, 0);
        }

        @Override
        int nextSetterIndex() {
            return setterIndex.get() != maxIndex ? setterIndex.getAndIncrement() : setAndGet(setterIndex, 0);
        }

        private int setAndGet(AtomicInteger o, int v) {
            o.set(v);
            return v;
        }
    }

    public static <T> LoopDeque<T> concurrentOfSize(int size) {
        return new ConcurrentLoopDeque<>(size);
    }
    /*private static final class NormalAlixPointerDeque<T> extends AlixPointerDeque<T> {

        private int getterIndex, setterIndex;

        private NormalAlixPointerDeque(int fixedSize) {
            super(fixedSize);
        }

        @Override
        int nextGetterIndex() {
            return getterIndex++;
        }

        @Override
        int nextSetterIndex() {
            return setterIndex++;
        }
    }*/

/*    public static <T> AlixPointerDeque<T> ofSize(int size) {
        return new NormalAlixPointerDeque(size);
    }*/
}