package shadow.utils.main.file.subtypes;

import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.FileManager;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.io.IOException;
import java.util.Map;

public class UserFile extends FileManager {

    public UserFile() {
        super("users.yml");
    }

    @Override
    protected void loadLine(String line) {
        PersistentUserData.from(line);
    }

    @Override
    public void save(Map<?, ?> map) {
        try {
            super.save(map);
        } catch (IOException e) {
            e.printStackTrace();
            Main.logWarning(AlixUtils.isPluginLanguageEnglish ? "Could not save users.yml file! Some information could be lost!" : "Nie udało się zapisać pliku users.yml! Pewne informacje mogły zostać utracone!");
        }
    }
}