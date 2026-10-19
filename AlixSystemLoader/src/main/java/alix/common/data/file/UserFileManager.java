package alix.common.data.file;

import alix.common.AlixCommonMain;
import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.connection.filters.PlayerNameIndex;
import alix.common.data.PersistentUserData;
import alix.common.database.DatabaseUpdater;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserFileManager {

    private static final Map<String, PersistentUserData> map = new ConcurrentHashMap<>();
    private static final UserFile file = new UserFile();
    private static final DatabaseUpdater database = DatabaseUpdater.INSTANCE;

    static {
        database.createTablesSync();
        try {
            file.load();//read the file

            var local = new HashSet<>(map.values());

            //supplementary data with database
            database.loadAllUsers(map).thenRun(() -> {
                file.save(map);//save all to local cache

                //local-only + db-only = all
                //local-only = all - db-only
                var dbOnly = new HashSet<>(map.values());
                dbOnly.removeAll(local);

                var localOnly = new HashSet<>(map.values());
                localOnly.removeAll(dbOnly);

                if (dbOnly.isEmpty()) return;

                String plural = localOnly.size() != 1 ? "s" : "";
                AlixCommonMain.logInfo("Saving " + localOnly.size() + " new user" + plural + " to the database");
                save(localOnly);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onAsyncSave() {
        save0();

        AlixCommonMain.debug("Successfully saved users.yml file!");
    }

    public static void fastSave() {
        save0();
    }

    static void save0() {
        file.save(map);
        //getAllData().stream().filter(data -> data.isDirty).forEach(PersistentUserData::saveToDatabase);
    }

    public static PersistentUserData get(String name) {
        return name != null ? map.get(name) : null;
    }

    //do not remove from UserTokensFileManager here - a premium name change reuses this same Identity for
    //the renamed record (see PersistentUserData#registerPremiumPlayerRename()), so wiping the 2FA secret/
    //recovery codes/encrypted email here would silently break that player's 2FA and lose their stored
    //email. Use removeFully() instead for an actual full account-data wipe (e.g. '/as fullyremovedata').
    public static PersistentUserData remove(String name) {
        PlayerNameIndex.remove(name);
        var data = map.remove(name);
        if (data != null) {
            //Caught rather than left to propagate: an exception here (e.g. from the Ataraxia IPC calls
            //GeoIPTracker may make) previously aborted this method before database.removeByName() ran,
            //silently leaving the row in the database - see removeFully()'s own docs for the same concern
            //one level up.
            try {
                GeoIPTracker.removeIP(data.getSavedIP());
            } catch (Exception e) {
                AlixCommonMain.logWarning("Failed to update GeoIPTracker while removing '" + name + "': " + e);
            }
            database.removeByName(name);
        }
        return data;
    }

    //Like remove(), but also wipes the 2FA secret, encrypted email backup and recovery codes -
    //removeByName() above only touches alix_users2/alix_passwords2, not alix_user_tokens. Only safe to use
    //when the Identity is truly being discarded (a full account-data wipe), never for a rename, which
    //reuses the same Identity for the new record.
    public static PersistentUserData removeFully(String name) {
        var data = remove(name);
        if (data != null) {
            //Caught rather than left to propagate: UserTokensFile#save() (called from removeTokenLocally())
            //wraps a real write failure as an unchecked RuntimeException - same class of bug as
            //GeoIPTracker's above, just one call deeper. Without this, a token-file write failure skipped
            //database.removeUserToken() AND fastSave() below, silently undoing part of a GDPR erasure.
            try {
                UserTokensFileManager.removeTokenLocally(data.identity());
            } catch (Exception e) {
                AlixCommonMain.logWarning("Failed to remove local 2FA token while removing '" + name + "': " + e);
            }
            database.removeUserToken(data.identity());
            //Force an immediate local-file flush rather than leaving this to the periodic (up to 1-minute)
            //autosave. Without this, a crash in that window leaves the erased account's row still sitting in
            //the on-disk users.yml - and the static initializer above treats any name present locally but
            //absent from the database as "local-only" and pushes it BACK into the database on the next boot,
            //silently undoing a completed GDPR erasure ('/as fullyremovedata').
            fastSave();
        }
        return data;
    }

    public static boolean hasName(String name) {
        return get(name) != null;
    }

    public static void putData(PersistentUserData data) {
        map.put(data.getName(), data);
        PlayerNameIndex.index(data.getName());
        //data.saveToDatabase();
    }

    public static Collection<PersistentUserData> getAllData() {
        return map.values();
    }

    public static void init() {
    }

    static void save(Collection<PersistentUserData> list) {
        list.forEach(PersistentUserData::saveToDatabase);
    }

    @SneakyThrows
    public static void saveLocalToDb() {
        if (!database.isImpl()) return;
        save(getAllData());
    }
}