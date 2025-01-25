package shadow.utils.objects.savable.data.storage;

import shadow.utils.main.AlixUtils;

import java.util.HashMap;
import java.util.Map;

public class StorageManager {

    private static final String storageListSeparator = "-~-";
    private final Map<String, Storage> map;

    //|data|this
    //this: storage-~-storage-~-storage
    //storage: name~-~val~-~val~-~val:
    public StorageManager() {
        this.map = new HashMap<>();
    }

    public StorageManager(String data) {
        this.map = new HashMap<>();
        if (data.equals("0")) return;
        String[] a = AlixUtils.split(data, storageListSeparator);
        for (String value : a) {
            Storage s = Storage.of(value);
            map.put(s.getName(), s);
        }
    }

    public String toSavable() {
        if (map.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        for (Storage s : map.values()) {
            sb.append(s.toSavable()).append(storageListSeparator);
        }
        return sb.substring(0, sb.length() - 3);//storageListSeparator.length() = 3
    }


    public boolean add(Storage storage) {
        String name = storage.getName();
        if (map.containsKey(name)) return false;
        map.put(name, storage);
        return true;
    }

    public boolean remove(String name) {
        return map.remove(name) != null;
    }

    public Storage get(String name) {
        return map.get(name);
    }
}