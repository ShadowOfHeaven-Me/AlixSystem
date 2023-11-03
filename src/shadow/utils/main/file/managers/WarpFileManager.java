package shadow.utils.main.file.managers;

import shadow.systems.commands.tab.subtypes.WarpCommandTabCompleter;
import shadow.utils.main.file.subtypes.WarpFile;
import shadow.utils.objects.savable.loc.NamedLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WarpFileManager {

    private static final Map<String, NamedLocation> map = new HashMap<>();
    private static final WarpFile file = new WarpFile();
    private static boolean hasChanged;

    public static void initialize() throws IOException {
        file.load();
    }

    public static void add(NamedLocation warp) {
        map.put(warp.getName(), warp);
        WarpCommandTabCompleter.add(warp.getName());
        hasChanged = true;
    }

    public static NamedLocation remove(String name) {
        NamedLocation NamedLocation = map.remove(name);
        if (NamedLocation != null) {
            WarpCommandTabCompleter.remove(name);
            hasChanged = true;
        }
        return NamedLocation;
    }

    public static void replace(NamedLocation warp) {
        map.replace(warp.getName(), warp);
    }

    public static void save() {
        if (!hasChanged) return;

        file.save(map);

        hasChanged = false;
    }

    public static NamedLocation get(String name) {
        return map.get(name);
    }
}