package alix.common.data.file;

import alix.common.AlixCommonMain;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.PersistentUserData;
import alix.common.utils.file.AlixFileManager;

import java.io.IOException;
import java.util.Map;

public final class UserFile extends AlixFileManager {

    UserFile() {
        super("users.yml");
    }

    @Override
    protected void loadLine(String line) {
        PersistentUserData data = PersistentUserData.from(line);
        UserFileManager.putData(data);
        GeoIPTracker.addIP(data.getSavedIP());//Add it here, as it was loaded
    }

    @Override
    public void save(Map<?, ?> map) {
        try {
            super.save(map);
        } catch (IOException e) {
            e.printStackTrace();
            AlixCommonMain.logWarning("Could not save users.yml file! Some information could be lost!");
        }
    }
}