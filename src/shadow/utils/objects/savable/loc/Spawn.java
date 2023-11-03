package shadow.utils.objects.savable.loc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import static shadow.utils.main.AlixUtils.*;

public class Spawn {

    private final Location location;

    public Spawn(Location location) {
        this.location = location;
        location.getWorld().setSpawnLocation(location);
    }

    public final Location getLocation() {
        return location;
    }

    @Override
    public final String toString() {
        return location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + ":" + location.getWorld().getName() + ":" + location.getYaw() + ":" + location.getPitch();
    }

    public static Spawn getDefaultSpawn() {
/*        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                return new Spawn(w.getSpawnLocation());
            }
        }*/
        World w = Bukkit.getWorlds().get(0);//The main world
        //Main.logInfo(isPluginLanguageEnglish ? "Could not find any normal-type worlds to set the default spawn! Improvising!" : "Nie znaleziono żadnych światów typu normalnego! Rozpoczęto improwizację!");
        return new Spawn(w.getSpawnLocation());
    }

    public static Spawn fromString(String a) {
        if (a == null || a.equals("null")) return getDefaultSpawn();
        String[] b = split(a, ':');
        World c = Bukkit.getWorld(b[3]);
        if (c == null) return getDefaultSpawn();
        return new Spawn(new Location(c, parseInteger(b[0]), parseInteger(b[1]), parseInteger(b[2]), Float.parseFloat(b[4]), Float.parseFloat(b[5])));
    }
}