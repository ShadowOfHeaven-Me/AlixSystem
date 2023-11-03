package shadow.utils.objects.savable.loc;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import static shadow.utils.main.AlixUtils.parsePureInteger;
import static shadow.utils.main.AlixUtils.split;

public class SavableLocation {

    public static String toSavableString(Location location) {
        return location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + ":" + location.getWorld().getName() + ":" + location.getYaw() + ":" + location.getPitch();
    }

    public static Location fromString(String a) {
        String[] b = split(a, ':');
        World c = Bukkit.getWorld((b[3]));
        if (c == null) return null;
        return new Location(c, parsePureInteger(b[0]), parsePureInteger(b[1]), parsePureInteger(b[2]), Float.parseFloat(b[4]), Float.parseFloat(b[5]));
    }
}
