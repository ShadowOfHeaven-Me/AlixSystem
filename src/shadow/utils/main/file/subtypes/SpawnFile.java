package shadow.utils.main.file.subtypes;

import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.FileManager;
import shadow.utils.objects.savable.loc.Spawn;

import java.io.IOException;

public class SpawnFile extends FileManager {

    private Spawn spawn = Spawn.getDefaultSpawn();//not-null
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

    public Spawn getSpawn() {
        return spawn;
    }

    public void setSpawn(Spawn spawn) {
        this.spawn = spawn;
    }
}