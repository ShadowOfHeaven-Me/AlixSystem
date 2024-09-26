package shadow.utils.main.file.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.subtypes.OriginalLocationsFile;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.world.AlixWorld;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public final class OriginalLocationsManager {

    private static final OriginalLocationsFile file = new OriginalLocationsFile();

/*    public static Location findOrCreateAndAdd(Player player) {
        UUID uuid = player.getUniqueId();
        return file.getMap().compute(uuid, (id, loc) -> loc != null ? loc : player.getLocation());
    }*/

    static {
        file.loadExceptionless();
    }

    public static Location getOriginalLocation(Player player) {
        Location originalLoc = file.getMap().get(player.getUniqueId());//no longer removing the saved location due to various issues
        return originalLoc != null && !originalLoc.getWorld().equals(AlixWorld.CAPTCHA_WORLD) ? originalLoc : SpawnFileManager.getSpawnLocation();//default to the spawn location if none was found (or it was in the captcha world)
    }

    public static CompletableFuture<Boolean> teleportBack(Player player) {
        return MethodProvider.teleportAsync(player, getOriginalLocation(player));
    }

    public static void add(Player player, @NotNull Location originalLocation) {
        if (originalLocation.getWorld().equals(AlixWorld.CAPTCHA_WORLD)) return;
        file.getMap().put(player.getUniqueId(), originalLocation);
        //if (originalLocation.getWorld().equals(AlixWorld.CAPTCHA_WORLD)) throw new AlixException(":>");
    }

/*    public static Location remove(Player player) {
        return file.getMap().remove(player.getUniqueId());
    }*/

    public static void onAsyncSave() {
        try {
            file.save();
            Main.logDebug(AlixUtils.isPluginLanguageEnglish ? "Successfully saved original-locations.txt file!" : "Poprawnie zapisano plik original-locations.txt!");
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