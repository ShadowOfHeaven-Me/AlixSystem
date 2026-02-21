package alix.common.data.file;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.database.DatabaseUpdater;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserFileManager {

    private static final Map<String, PersistentUserData> map = new ConcurrentHashMap<>();
    private static final UserFile file = new UserFile();
    private static final DatabaseUpdater database = DatabaseUpdater.INSTANCE;

    static {
        database.createTablesSync();
        try {
            file.load();//The file loads
            file.save(map);//The missing data is updated, thus saving the file prevents further errors
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onAsyncSave() {
        file.save(map);

        AlixCommonMain.debug("Successfully saved users.yml file!");
    }

    public static void fastSave() {
        file.save(map);
    }

    public static PersistentUserData get(String name) {
        return name != null ? map.get(name) : null;
    }

    public static PersistentUserData remove(String name) {
        return map.remove(name);
    }

    public static boolean hasName(String name) {
        return get(name) != null;
    }

    /*public static PersistentUserData getOrCreatePremiumInformation(String name, InetAddress ip) {
        PersistentUserData data = get(name);

        return data != null ? data : PersistentUserData.createFromPremiumInfo(name, ip);
    }*/

    public static void putData(PersistentUserData data) {
        map.put(data.getName(), data);
        //data.saveToDatabase();
    }

    public static Collection<PersistentUserData> getAllData() {
        return map.values();
    }

    public static void init() {
    }
}