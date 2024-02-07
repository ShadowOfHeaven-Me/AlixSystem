package shadow.utils.objects.savable.loc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.file.managers.SpawnFileManager;

import static shadow.utils.main.AlixUtils.parsePureInteger;
import static shadow.utils.main.AlixUtils.split;

public class NamedLocation {//extends SavableLocation {

    private final String name, toString, readable;
    private final Location loc;

    public NamedLocation(Location loc, String name) {
        //super(location);
        this.name = name;
        this.loc = loc;
        //toString = super.toString() + ":" + name;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        String worldName = loc.getWorld().getName();
        this.toString = x + ":" + y + ":" + z + ":" + worldName + ":" + loc.getYaw() + ":" + loc.getPitch()+ ":" + name;
        this.readable = name + " X: " + x + " Y: " + y + " Z: " + z + ' ' + worldName;
    }

    @Override
    public final String toString() {
        return toString;
    }

    public final String toUserReadable() {
        return readable;
    }

    public final String getName() {
        return name;
    }

/*    public static int indexOf(NamedLocation[] a, String b) {
        for (int i = 0; i < a.length; i++)
            if (a[i].name.equals(b)) return i;
        return -1;
    }*/

    public static NamedLocation fromString(String a) {
        String[] b = split(a, ':');
        World c = Bukkit.getWorld((b[3]));
        if (c == null) return null;
        return new NamedLocation(new Location(c, parsePureInteger(b[0]), parsePureInteger(b[1]), parsePureInteger(b[2]), Float.parseFloat(b[4]), Float.parseFloat(b[5])), b[6]);
    }

    public void teleport(Player p) {
        MethodProvider.teleportAsync(p, loc);
    }
}