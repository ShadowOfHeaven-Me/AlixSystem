package shadow.utils.main.file.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.subtypes.OriginalLocationsFile;

import java.io.IOException;

public final class OriginalLocationsManager {

    private static final OriginalLocationsFile file = new OriginalLocationsFile();

/*    public static Location findOrCreateAndAdd(Player player) {
        UUID uuid = player.getUniqueId();
        return file.getMap().compute(uuid, (id, loc) -> loc != null ? loc : player.getLocation());
    }*/

    static {
        try {
            file.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void teleportBack(Player player, boolean warningIfAbsent) {
        Location originalLoc = file.getMap().get(player.getUniqueId());//no longer removing the saved location due to various issues
        if (originalLoc != null) MethodProvider.teleportAsync(player, originalLoc);
        else if (warningIfAbsent) {
            Main.logWarning("The original location was absent! - The Player was in the captcha world before verification! Teleporting the player to the default spawn!");
            MethodProvider.teleportAsync(player, SpawnFileManager.getSpawnLocation());
        }
    }

    public static void add(Player player, @NotNull Location originalLocation) {
        file.getMap().put(player.getUniqueId(), originalLocation);
    }

/*    public static Location remove(Player player) {
        return file.getMap().remove(player.getUniqueId());
    }*/

    public static void onAsyncSave() {
        try {
            file.save();
            Main.debug(AlixUtils.isPluginLanguageEnglish ? "Successfully saved original-locations.txt file!" : "Poprawnie zapisano plik original-locations.txt!");
        } catch (IOException e) {
            e.printStackTrace();
            Main.logWarning(AlixUtils.isPluginLanguageEnglish ? "Could not save original-locations.txt file! Some information could be lost!" : "Nie udało się zapisać pliku original-locations.txt! Pewne informacje mogły zostać utracone!");
        }
    }

    public static void fastSave() {
        try {
            file.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
    }
}