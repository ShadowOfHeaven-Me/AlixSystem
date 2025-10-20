package shadow.systems.login.autoin.premium;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

public class NetworkManagerList<T> implements List<T> {

    private final List<T> delegate;

    public NetworkManagerList(List<T> delegate) {
        this.delegate = delegate;
    }

    private void onAdd0(T t) {
        AuthReflection.onAdd0(t);
    }

    @Override
    public void add(int index, T element) {
        delegate.add(index, element);
        this.onAdd0(element);
    }

    @Override
    public boolean add(T t) {
        this.onAdd0(t);
        return delegate.add(t);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        c.forEach(this::onAdd0);
        return delegate.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        c.forEach(this::onAdd0);
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void replaceAll(@NotNull UnaryOperator<T> operator) {
        delegate.replaceAll(operator);
    }

    @Override
    public void sort(@Nullable Comparator<? super T> c) {
        delegate.sort(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public T get(int index) {
        return delegate.get(index);
    }

    @Override
    public T set(int index, T element) {
        return delegate.set(index, element);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public T remove(int index) {
        return delegate.remove(index);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public @NotNull ListIterator<T> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    public @NotNull List<T> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    @Override
    public @NotNull Spliterator<T> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public @NotNull Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public @NotNull <T1> T1[] toArray(@NotNull T1[] a) {
        return delegate.toArray(a);
    }

    @SuppressWarnings("SlowListContainsAll")
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }
}