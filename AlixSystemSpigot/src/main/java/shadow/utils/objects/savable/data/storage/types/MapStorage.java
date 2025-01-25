package shadow.utils.objects.savable.data.storage.types;

import shadow.utils.objects.savable.data.storage.Storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class MapStorage<K, V> extends Storage {

    private final Map<K, V> map;

    public MapStorage(String name) {
        super(name);
        this.map = new HashMap<>();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    public Map<K, V> getMap() {
        return map;
    }
}