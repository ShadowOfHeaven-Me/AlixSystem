package alix.common.data.file;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.utils.file.AlixFileManager;

import java.io.IOException;
import java.util.Map;

public final class UserFile extends AlixFileManager {

    UserFile() {
        super("users.yml", FileType.INTERNAL);
    }

    @Override
    protected void loadLine(String line) {
        PersistentUserData.from(line);
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