package alix.common.utils.collections.queue.array;

public abstract class AlixArrayDeque<T> {

    private final Element<T>[] array;

    private AlixArrayDeque(int fixedSize) {
        this.array = new Element[fixedSize];
        for (int i = 0; i < array.length; i++) this.array[i] = new Element<>();
    }

    public T pollFirst() {
        Element<T> o = this.array[this.currentGetterIndex()];
        synchronized (o) {
            this.array[this.currentSetterIndex()].value = null;
        }
        return o.value;
    }

    abstract int currentGetterIndex();

    abstract int currentSetterIndex();

    abstract int nextGetterIndex();

    abstract int nextSetterIndex();

    private static final class Element<T> {

        private T value;

    }
}