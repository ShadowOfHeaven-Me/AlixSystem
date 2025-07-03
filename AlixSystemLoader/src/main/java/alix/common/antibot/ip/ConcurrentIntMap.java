package alix.common.antibot.ip;

import io.netty.util.collection.IntObjectMap;
import io.netty.util.internal.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static alix.common.utils.collections.list.AtomicArrayUtil.getElement;
import static alix.common.utils.collections.list.AtomicArrayUtil.setElement;

public final class ConcurrentIntMap<V> implements IntObjectMap<V> {

    private int[] keys;
    private V[] values;
    private int mask;

    public ConcurrentIntMap(int size) {
        this.initTab(MathUtil.safeFindNextPositivePowerOfTwo(size));
    }

    @SuppressWarnings("SuspiciousArrayCast")
    private void initTab(int powOf2Size) {
        this.values = (V[]) new Object[powOf2Size];
        this.keys = new int[powOf2Size];
        this.mask = powOf2Size - 1;
    }

    @Override
    public V get(int key) {

        return null;
    }

    private void grow() {

    }

    @Override
    public V put(int key, V value) {
        int idx = key & mask;
        while (true) {
            if (getElement(this.values, idx) == null) {
                setElement(this.values, idx, value);
                setElement(this.keys, idx, key);
                this.grow();
                return null;
            }
            /*if (getElement(this.keys, idx) == key) {
                // Found existing entry with this key, just replace the value.
                V previousValue = values[index];
                values[index] = toInternal(value);
                return toExternal(previousValue);
            }*/
        }
    }

    @Override
    public V remove(int key) {
        return null;
    }

    @Override
    public Iterable<PrimitiveEntry<V>> entries() {
        return null;
    }

    @Override
    public boolean containsKey(int key) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public @Nullable V put(Integer key, V value) {
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public @NotNull Set<Integer> keySet() {
        return Set.of();
    }

    @Override
    public @NotNull Collection<V> values() {
        return List.of();
    }

    @Override
    public @NotNull Set<Entry<Integer, V>> entrySet() {
        return Set.of();
    }
}