package alix.velocity.utils.data;

import alix.velocity.utils.data.file.UserDataFile;

import java.io.IOException;

public final class UserDataManager {

    private static final UserDataFile file = new UserDataFile();

    public static void load() {
        try {
            file.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PersistentUserData get(String name) {
        return file.getMap().get(name);
    }
}