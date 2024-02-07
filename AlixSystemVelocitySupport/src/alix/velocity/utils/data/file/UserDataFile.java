package alix.velocity.utils.data.file;

import alix.common.utils.file.FileManager;
import alix.velocity.utils.data.PersistentUserData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserDataFile extends FileManager {

    private final Map<String, PersistentUserData> map = new ConcurrentHashMap<>();

    public UserDataFile() {
        super("users.yml");
    }

    @Override
    protected void loadLine(String line) {
        String[] data = line.split("\\|");
        this.map.put(data[0], PersistentUserData.fromData(data));
    }

    public final Map<String, PersistentUserData> getMap() {
        return map;
    }
}