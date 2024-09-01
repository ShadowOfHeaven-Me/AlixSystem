package alix.common.data.loc.impl.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class BukkitNamedLocation {//extends SavableLocation {

    private final String name, toString, readable;
    private final Location loc;

    public BukkitNamedLocation(Location loc, String name) {
        this.name = name;
        this.loc = loc;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        String worldName = loc.getWorld().getName();
        this.toString = x + ":" + y + ":" + z + ":" + worldName + ":" + loc.getYaw() + ":" + loc.getPitch()+ ":" + name;
        this.readable = name + " X: " + x + " Y: " + y + " Z: " + z + ' ' + worldName;
    }

    @Override
    public String toString() {
        return toString;
    }

    public String toUserReadable() {
        return readable;
    }

    public String getName() {
        return name;
    }

/*    public static int indexOf(NamedLocation[] a, String b) {
        for (int i = 0; i < a.length; i++)
            if (a[i].name.equals(b)) return i;
        return -1;
    }*/

    public static BukkitNamedLocation fromString(String a) {
        String[] b = a.split(":");
        World c = Bukkit.getWorld((b[3]));
        if (c == null) return null;
        return new BukkitNamedLocation(new Location(c, Integer.parseInt(b[0]), Integer.parseInt(b[1]), Integer.parseInt(b[2]), Float.parseFloat(b[4]), Float.parseFloat(b[5])), b[6]);
    }

    public void teleport(Player p) {
        //PaperLib.teleportAsync(p, loc);
        p.teleport(loc);
    }
}