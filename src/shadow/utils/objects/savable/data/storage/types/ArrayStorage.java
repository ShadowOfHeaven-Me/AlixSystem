package shadow.utils.objects.savable.data.storage.types;

import shadow.utils.objects.savable.data.storage.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ArrayStorage<V> extends Storage {

    private final List<V> list;

    public ArrayStorage(String name) {
        super(name);
        this.list = new ArrayList<>();
    }

    @Override
    public Collection<?> values() {
        return list;
    }

    public List<V> getList() {
        return list;
    }
}