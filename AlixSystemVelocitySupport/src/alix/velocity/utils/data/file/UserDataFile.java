package alix.velocity.utils.data.file;

import alix.common.utils.file.AlixFileManager;
import alix.velocity.utils.data.PersistentUserData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserDataFile extends AlixFileManager {

    private final Map<String, PersistentUserData> map = new ConcurrentHashMap<>();

    public UserDataFile() {
        super("users.yml", FileType.INTERNAL);
    }

    @Override
    protected void loadLine(String line) {
        String[] data = line.split("\\|");
        this.map.put(data[0], PersistentUserData.fromData(data));
    }

    public Map<String, PersistentUserData> getMap() {
        return map;
    }
}