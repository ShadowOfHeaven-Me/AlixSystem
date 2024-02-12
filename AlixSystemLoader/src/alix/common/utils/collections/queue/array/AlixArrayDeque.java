package alix.common.utils.collections.queue.array;

import java.util.ArrayDeque;

public class AlixArrayDeque<T> {

    private final Object[] array;

    public AlixArrayDeque(int fixedSize) {
        this.array = new Object[fixedSize];
    }

    public T pollFirst() {
        Object o = array[0];
        System.arraycopy(array, 1, array, 0, array.length - 1);
        return (T) o;
    }



}