package shadow.utils.main.file.subtypes;

import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.FileManager;
import shadow.utils.objects.savable.loc.Spawn;

import java.io.IOException;

public final class SpawnFile extends FileManager {

    private Spawn spawn = Spawn.DEFAULT_SPAWN;//not-null
    private byte index;

    public SpawnFile() {
        super("spawn.yml");
    }

    @Override
    protected void loadLine(String line) {
/*        if (spawn != null) throw new RuntimeException("Spawn tried to load in twice!");
        spawn = Spawn.fromString(line);*/
        if (index++ == 0) {
            spawn = Spawn.fromString(line);
        }
    }
                /*            case 1:
                Password.setEncrypt(JavaUtils.parseInteger(line));
                break;*/

    public void save() {
        try {
            saveSingularValue(spawnSavable());//, Password.generateNewEncryption()));
        } catch (IOException e) {
            e.printStackTrace();
            Main.logWarning(AlixUtils.isPluginLanguageEnglish ? "Could not save spawn.yml file! Some information could be lost!" : "Nie udało się zapisać pliku spawn.yml! Pewne informacje mogły zostać utracone!");
        }
    }

    private String spawnSavable() {
        return String.valueOf(spawn);
    }

    @NotNull
    public Spawn getSpawn() {
        return spawn;
    }

    public void setSpawn(Spawn spawn) {
        this.spawn = spawn;
    }
}