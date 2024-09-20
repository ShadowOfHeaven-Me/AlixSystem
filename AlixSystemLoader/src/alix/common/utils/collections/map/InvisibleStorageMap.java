package alix.common.utils.collections.map;

import java.util.*;

//Stores all that was added, but claims it's an empty map
//The stored contents can only be accessed via getStorage()
public final class InvisibleStorageMap implements Map {

    private static final Map EMPTY_MAP = Collections.EMPTY_MAP;
    private final Map storage;

    public InvisibleStorageMap(Map storage) {
        this.storage = storage;
    }

    public Map getStorage() {
        return storage;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
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
    public Object get(Object key) {
        return null;
    }

    @Override
    public Object put(Object key, Object value) {
        storage.put(key, value);
        return null;
    }

    @Override
    public Object remove(Object key) {
        storage.remove(key);
        return null;
    }

    @Override
    public void putAll(Map m) {
        storage.putAll(m);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public Set keySet() {
        return EMPTY_MAP.keySet();
    }

    @Override
    public Collection values() {
        return EMPTY_MAP.values();
    }

    @Override
    public Set<Entry> entrySet() {
        return EMPTY_MAP.entrySet();
    }
}