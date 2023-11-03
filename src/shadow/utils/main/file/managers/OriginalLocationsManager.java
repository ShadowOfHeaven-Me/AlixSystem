package shadow.utils.main.file.managers;

import alix.common.scheduler.impl.AlixScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.subtypes.OriginalLocationsFile;

import java.io.IOException;

public class OriginalLocationsManager {

    private static final OriginalLocationsFile file = new OriginalLocationsFile();

/*    public static Location findOrCreateAndAdd(Player player) {
        UUID uuid = player.getUniqueId();
        return file.getMap().compute(uuid, (id, loc) -> loc != null ? loc : player.getLocation());
    }*/

    public static void add(Player player, Location originalLocation) {
        file.getMap().put(player.getUniqueId(), originalLocation);
    }

    public static Location remove(Player player) {
        return file.getMap().remove(player.getUniqueId());
    }

    public static void initialize() throws IOException {
        file.load();
    }

    public static void asyncSave() {
        AlixScheduler.async(() -> {
            try {
                file.save();
                Main.debug(AlixUtils.isPluginLanguageEnglish ? "Successfully saved original-locations.txt file!" : "Poprawnie zapisano plik original-locations.txt!");
            } catch (IOException e) {
                e.printStackTrace();
                Main.logWarning(AlixUtils.isPluginLanguageEnglish ? "Could not save original-locations.txt file! Some information could be lost!" : "Nie udało się zapisać pliku original-locations.txt! Pewne informacje mogły zostać utracone!");
            }
        });
    }

    public static void fastSave() {
        try {
            file.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}