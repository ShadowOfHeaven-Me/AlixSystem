package shadow.utils.main.file.subtypes;

import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.FileManager;
import shadow.utils.main.file.managers.WarpFileManager;
import alix.common.data.loc.impl.bukkit.BukkitNamedLocation;

import java.io.IOException;
import java.util.Map;

public class WarpsFile extends FileManager {

    public WarpsFile() {
        super("warps.yml", FileType.INTERNAL);
    }

    @Override
    protected void loadLine(String line) {
        WarpFileManager.add(BukkitNamedLocation.fromString(line));
    }

    @Override
    public void save(Map<?, ?> map) {
        try {
            super.save(map);
            Main.debug(AlixUtils.isPluginLanguageEnglish ? "Successfully saved warps.yml file!" : "Poprawnie zapisano plik warps.yml!");
        } catch (IOException e) {
            e.printStackTrace();
            Main.logWarning(AlixUtils.isPluginLanguageEnglish ? "Could not save warps.yml file! Some information could be lost!" : "Nie udało się zapisać pliku warps.yml! Pewne informacje mogły zostać utracone!");
        }
    }
}