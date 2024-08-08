package shadow.utils.main.file.managers;

import shadow.systems.commands.tab.subtypes.WarpCommandTabCompleter;
import shadow.utils.main.file.subtypes.WarpFile;
import alix.common.data.loc.impl.bukkit.BukkitNamedLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class WarpFileManager {

    private static final Map<String, BukkitNamedLocation> map = new HashMap<>();
    private static final WarpFile file = new WarpFile();
    private static boolean hasChanged;

    public static void initialize() throws IOException {
        file.load();
    }

    public static void add(BukkitNamedLocation warp) {
        map.put(warp.getName(), warp);
        WarpCommandTabCompleter.add(warp.getName());
        hasChanged = true;
    }

    public static BukkitNamedLocation remove(String name) {
        BukkitNamedLocation BukkitNamedLocation = map.remove(name);
        if (BukkitNamedLocation != null) {
            WarpCommandTabCompleter.remove(name);
            hasChanged = true;
        }
        return BukkitNamedLocation;
    }

    public static void replace(BukkitNamedLocation warp) {
        map.replace(warp.getName(), warp);
    }

    public static void save() {
        if (!hasChanged) return;

        file.save(map);

        hasChanged = false;
    }

    public static BukkitNamedLocation get(String name) {
        return map.get(name);
    }
}