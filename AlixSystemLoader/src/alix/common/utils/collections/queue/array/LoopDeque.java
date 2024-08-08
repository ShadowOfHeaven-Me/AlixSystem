package alix.common.utils.collections.queue.array;

import alix.common.utils.collections.list.LoopList;
import alix.common.utils.other.annotation.OptimizationCandidate;

import java.util.function.Consumer;

public final class LoopDeque<T> {

    private final LoopList<T> list;

    private LoopDeque(LoopList<T> list) {
        this.list = list;
    }

    //Gets the next value and reassigns the address it was taken from to the 'replacement' var provided
    //Can be optimized by replacing the index get with a single call
    @OptimizationCandidate
    public T getNextAndReplace(T replacement) {
        int i = this.list.nextIndex();
        T next = this.list.get(i);
        this.list.setValue(i, replacement);
        return next;
    }

    public void offerNext(T t) {
        this.list.setNext(t);
    }

    public void forEachNonNull(Consumer<T> consumer) {
        for (Object o : this.list.getValues()) if (o != null) consumer.accept((T) o);
    }

    public static <T> LoopDeque<T> ofSize(int size) {
        return new LoopDeque<>(LoopList.ofSize(size));
    }

    public static <T> LoopDeque<T> concurrentOfSize(int size) {
        return new LoopDeque<>(LoopList.newConcurrentOfSize(size));
    }

/*    private static final class NormalLoopDeque<T> extends LoopDeque<T> {

        //The underlying worker LoopList
        private final LoopList<T> list;

        private NormalLoopDeque(int fixedSize) {
            super(fixedSize);
            this.list = LoopList.ofSize(fixedSize);
        }

        @Override
        public T getNextAndReplace(T replacement) {
            int i = this.list.getCurrentIndex();
            T next = this.list.next();
            this.list.setValue(i, replacement);
            return next;
        }

        @Override
        public void offerNext(T t) {
            this.list.setNext(t);
        }
    }*/

/*    private static final class ConcurrentLoopDeque<T> extends LoopDeque<T> {

        private final AtomicInteger getterIndex, setterIndex;
        private final int maxIndex;

        private ConcurrentLoopDeque(int fixedSize) {
            super(fixedSize);
            this.getterIndex = new AtomicInteger();
            this.setterIndex = new AtomicInteger();
            this.maxIndex = fixedSize - 1;
        }

        @Override
        int nextIndex() {
            return LoopList.nextLoopIndex(this.getterIndex, this.maxIndex);
        }

        @Override
        int nextSetterIndex() {
            return LoopList.nextLoopIndex(this.setterIndex, this.maxIndex);
        }
    }*/


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