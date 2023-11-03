package shadow.utils.objects.savable.data.storage.types;

import shadow.utils.objects.savable.data.storage.Storage;

import java.util.Collection;
import java.util.Collections;

public abstract class SingleValueStorage<V> extends Storage {

    private V value;

    public SingleValueStorage(String name) {
        super(name);
    }

    @Override
    public Collection<?> values() {
        return Collections.singletonList(value);
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
