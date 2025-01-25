package shadow.utils.objects.savable.loc;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import shadow.utils.main.file.managers.SpawnFileManager;

import static shadow.utils.main.AlixUtils.split;

public final class SavableLocation {

    public static String toSavableString(Location loc) {
        return loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + loc.getWorld().getName() + ":" + loc.getYaw() + ":" + loc.getPitch();
    }

    @NotNull
    public static Location fromStringOrSpawnIfAbsent(String a) {
        Location loc = fromString(a);
        return loc != null ? loc : SpawnFileManager.getSpawnLocation();
    }

    public static Location fromString(String a) {
        String[] b = split(a, ':');
        World c = Bukkit.getWorld((b[3]));
        if (c == null) return null;
        return new Location(c, Integer.parseInt(b[0]), Integer.parseInt(b[1]), Integer.parseInt(b[2]), Float.parseFloat(b[4]), Float.parseFloat(b[5]));
    }

    private SavableLocation() {
    }
}
