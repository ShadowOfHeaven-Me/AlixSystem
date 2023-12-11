package shadow.utils.main.file.managers;

import org.bukkit.entity.Player;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.subtypes.UserFile;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class UserFileManager {

    private static final Map<String, PersistentUserData> map = new HashMap<>();
    private static final UserFile file = new UserFile();

    public static void initialize() throws IOException {
        file.load();//The file loads
        file.save(map);//The missing data is updated, thus saving the file prevents further errors
    }

    public static void onAsyncSave() {
        file.save(map);

        Main.debug(AlixUtils.isPluginLanguageEnglish ? "Successfully saved users.yml file!" : "Poprawnie zapisano plik users.yml!");
    }

    public static void fastSave() {
        file.save(map);
    }

    public static PersistentUserData get(String name) {
        return name != null ? map.get(name) : null;
    }

    public static boolean hasName(String name) {
        return get(name) != null;
    }

    public static PersistentUserData getOrCreatePremiumInformation(Player p) {
        PersistentUserData data = get(p.getName());

        return data != null ? data : PersistentUserData.createFromPremiumPlayer(p);
    }

    public static void addData(PersistentUserData data) {
        map.put(data.getName(), data);
    }

    public static Collection<PersistentUserData> getAllData() {
        return map.values();
    }
}