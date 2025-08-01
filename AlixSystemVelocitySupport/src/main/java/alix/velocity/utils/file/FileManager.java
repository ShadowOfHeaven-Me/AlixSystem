package alix.velocity.utils.file;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.data.file.UserFileManager;
import alix.common.scheduler.AlixScheduler;

import java.util.concurrent.TimeUnit;

public final class FileManager {

    private static final boolean antiBotService = true;

    public static void loadFiles() {
        UserFileManager.init();
        UserTokensFileManager.init();
        if (antiBotService) FireWallManager.init();

        AlixScheduler.repeatAsync(FileManager::onAsyncSave, 1, TimeUnit.MINUTES);
    }

    private static void onAsyncSave() {
        UserFileManager.onAsyncSave();
        UserTokensFileManager.save();
        if (antiBotService) FireWallManager.onAsyncSave();
    }

    public static void saveFiles() {
        UserFileManager.fastSave();
        UserTokensFileManager.save();
        if (antiBotService) FireWallManager.fastSave();
    }
}