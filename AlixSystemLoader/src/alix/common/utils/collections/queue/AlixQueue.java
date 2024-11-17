package alix.common.utils.collections.queue;

import java.util.function.Consumer;

public interface AlixQueue<T> {

    T pollFirst();

    void offerLast(T element);

    void clear();

    void forEach(Consumer<T> action);

}