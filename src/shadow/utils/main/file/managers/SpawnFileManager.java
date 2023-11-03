package shadow.utils.main.file.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.file.subtypes.SpawnFile;
import shadow.utils.objects.savable.loc.Spawn;

import java.io.IOException;

public class SpawnFileManager {

    private static final SpawnFile file = new SpawnFile();

    public static void save() {
        file.save();
    }

    public static void set(Location l) {
        file.setSpawn(new Spawn(l));
    }

    public static Location getSpawnLocation() {
        return file.getSpawn().getLocation();
    }

/*    public static void teleport(Player p) {
        AlixHandler.delayedConfigTeleport(p, file.getSpawn().getLocation());
    }*/

    public static void initialize() throws IOException {
        file.load();
    }
}