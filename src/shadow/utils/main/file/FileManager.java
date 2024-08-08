package shadow.utils.main.file;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.Messages;
import alix.common.utils.file.AlixFileManager;
import alix.common.utils.file.managers.IpsCacheFileManager;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.main.file.managers.SpawnFileManager;
import alix.common.data.file.UserFileManager;
import shadow.utils.main.file.managers.WarpFileManager;

import java.io.File;
import java.io.IOException;

public abstract class FileManager extends AlixFileManager {

    protected FileManager(File file) {
        super(file);
    }

    protected FileManager(String fileName) {
        super(fileName);
    }

    protected FileManager(String fileName, boolean internal) {
        super(fileName, internal);
    }

    protected FileManager(String fileName, boolean init, boolean internal) {
        super(fileName, init, internal);
    }

    public static void loadFiles() {
        try {
            Messages.init();
            UserFileManager.init();
            FireWallManager.init();
            IpsCacheFileManager.init();
            WarpFileManager.initialize();
            OriginalLocationsManager.init();
            SpawnFileManager.initialize();
            Main.logInfo(AlixUtils.isPluginLanguageEnglish ? "All files were successfully loaded!" : "Poprawnie wczytano wszystkie pliki!");
        } catch (IOException e) {
            Main.logError(AlixUtils.isPluginLanguageEnglish ? "An error occurred whilst trying to load the " + e.getMessage() + " file!"
                    : "Napotkano error przy próbie wczytania pliku " + e.getMessage() + "!");
            e.getCause().printStackTrace();
        }
    }

    public static void saveFiles() {
        UserFileManager.fastSave();
        OriginalLocationsManager.fastSave();
        FireWallManager.fastSave();
        WarpFileManager.save();
        SpawnFileManager.save();
    }
}