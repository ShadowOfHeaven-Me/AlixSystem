package alix.common.utils.collections.queue;

import alix.common.utils.AlixCommonUtils;

import java.util.function.Consumer;

public class AlixDeque<T> {

    private Node<T> first;
    private Node<T> last;
    private int size;

    public void offerLast(T element) {
        this.addNodeLast(new Node<>(element));
    }

    public T pollFirst() {
        if (first == null) return null;
        T element = first.element;
        this.first = first.next;
        this.size--;
        return element;
    }

    public void clear() {
        this.first = this.last = null;
        this.size = 0;
    }

/*    public void setFirstNode0(Node<T> node) {

    }*/

    public final T firstElement0() {
        return first.element;
    }

    public final T peekFirst() {
        return first != null ? first.element : null;
    }

    public final T peekLast() {
        return last != null ? last.element : null;
    }

    public final boolean isEmpty() {
        return this.first == null;
    }

    public final Node<T> firstNode() {
        return first;
    }

/*    public void addAll(AlixDeque<T> deque) {
        this.addNodeLast(deque.first);
    }*/

    public final int size() {
        return size;
    }

    public void addNodeLast(Node<T> node) {
        if (node == null) return;
        if (first == null) this.first = this.last = node;
        else this.last = this.last.next = node;
        this.size++;
    }

    public final void forEach(Consumer<T> consumer) {
        forEach(consumer, this);
    }

    public static <T> void forEach(Consumer<T> consumer, AlixDeque<T> deque) {
        forEach(consumer, deque.firstNode());
    }

    public static <T> void forEach(Consumer<T> consumer, Node<T> node) {
        while (node != null) {
            try {
                consumer.accept(node.element);
            } catch (Exception e) {
                AlixCommonUtils.logException(e);
            }
            node = node.next;
        }
    }

    public static final class Node<T> {

        public final T element;
        private Node<T> next;

        private Node(T element) {
            this.element = element;
        }
    }
}