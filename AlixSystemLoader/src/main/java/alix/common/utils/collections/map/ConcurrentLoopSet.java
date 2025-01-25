package alix.common.utils.collections.map;

import alix.common.utils.collections.list.LoopList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConcurrentLoopSet<T> {

    private static final Boolean PRESENT = Boolean.TRUE;
    private final Map<T, Boolean> map;
    private final LoopList<T> list;

    public ConcurrentLoopSet(int fixedSize) {
        this.map = new ConcurrentHashMap<>(fixedSize);
        this.list = LoopList.newConcurrentOfSize(fixedSize);
    }

    //Returns true if the key existed
    public boolean putNext(T key) {
        T previous = this.list.current();
        if (previous != null) this.map.remove(previous);
        this.list.setNext(key);
        return this.map.put(key, PRESENT) != null;
    }
}