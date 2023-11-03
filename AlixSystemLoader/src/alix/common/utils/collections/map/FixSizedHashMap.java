package alix.common.utils.collections.map;

import alix.common.utils.AlixCommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class FixSizedHashMap<K, V> implements AlixMap<K, V> {

    /**
     * A custom Key-Value implementation of AlixMap, that requires all
     * of it's modifications to be made synchronously, but allows
     * asynchronous iteration.
     *
     * @author ShadowOfHeaven
     **/

    private final Itr iterator = new Itr();//create the iterator, since it's main appeal is the possible async iteration
    private final NodeMapQueue<K, V>[] table;
    private final int tableSize;

    public FixSizedHashMap(int tableSize) {
        this.tableSize = AlixCommonUtils.nextPrimeEqualOrGreaterThan(tableSize);//make the size a prime number in order to avoid hash collisions as much as possible
        this.table = NodeMapQueue.construct(this.tableSize);
    }

    @Override
    public void put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException();

        int hashCode = key.hashCode();
        this.getNode(hashCode).put(key, value, hashCode);
    }

    @Override
    public V get(K key) {
        if (key == null) throw new NullPointerException();

        int hashCode = key.hashCode();
        return this.getNode(hashCode).get(key, hashCode);
    }

    @Override
    public V remove(K key) {
        if (key == null) throw new NullPointerException();

        int hashCode = key.hashCode();
        return this.getNode(hashCode).remove(key, hashCode);
    }

    @Override
    public Iterator<V> iterator() {
        return iterator;
    }

    @Override
    public void forEach(Consumer<? super V> action) {
        new Itr().forEachRemaining(action);//construct a new value iterator as the default one can already be used for iterating
    }

    @Override
    public void forEach(BiConsumer<K, V> consumer) {//construct a new value iterator as the default one can already be used for iterating
        Itr itr = new Itr();
        while (itr.hasNext()) {
            consumer.accept(itr.currentNode.key, itr.currentNode.value);
        }
    }

    private NodeMapQueue<K, V> getNode(int hashCode) {
        return this.table[(hashCode >>> 1) & this.tableSize];//get a non-negative index within the bounds
    }

    private final class Itr implements Iterator<V> {

        private Node<K, V> currentNode;
        private int index;
        private boolean advanced;

        @Override
        public boolean hasNext() {
            this.advance();
            return currentNode != null;
        }

        @Override
        public V next() throws NoSuchElementException {
            if (!advanced) this.advance();
            if (currentNode == null) throw new NoSuchElementException();
            this.advanced = false;
            return currentNode.value;
        }

        private void advance() {
            this.advanced = true;
            if (currentNode != null) {
                Node<K, V> next = currentNode.next;
                if (next != null) {
                    this.currentNode = next;
                    return;
                }
            }
            for (int i = index; i < tableSize; i++) {
                NodeMapQueue<K, V> node = table[i];
                Node<K, V> first = node.first;
                if (first != null) {
                    this.currentNode = first;
                    this.index = i + 1;
                    break;
                }
            }
        }
    }

    private static final class NodeMapQueue<K, V> {

        private Node<K, V> first, last;

        private NodeMapQueue() {

        }

        private void put(K key, V value, int hashCode) {
            if (this.first == null) this.first = this.last = new Node<>(key, value, hashCode);
            else this.last = this.last.next = new Node<>(key, value, hashCode);
        }

        /**
         * @noinspection ObjectEquality
         */
        private V get(K key, int hashCode) {
            if (this.first == null) return null;
            Node<K, V> current = this.first;

            do {
                if (current.hashCode == hashCode && current.key.equals(key)) return current.value;
            } while ((current = current.next) != null);

            return null;
        }

        /**
         * @noinspection ObjectEquality
         */
        private V remove(K key, int hashCode) {
            if (this.first == null) return null;
            Node<K, V> previous = null;
            Node<K, V> current = this.first;

            do {
                if (current.hashCode == hashCode && current.key.equals(key)) {
                    if (previous == null) this.first = current.next;
                    else previous.next = current.next;
                    return current.value;
                }
                previous = current;
            } while ((current = current.next) != null);
            return null;
        }

        private static <K, V> NodeMapQueue<K, V>[] construct(int size) {
            NodeMapQueue<K, V>[] array = new NodeMapQueue[size];

            AlixCommonUtils.fillArray(array, NodeMapQueue::new);

            return array;
        }

    }

    private static final class Node<K, V> {

        private final K key;
        private final V value;
        private final int hashCode;
        private Node<K, V> next;

        private Node(K key, V value, int hashCode) {
            this.key = key;
            this.value = value;
            this.hashCode = hashCode;
        }
    }
}