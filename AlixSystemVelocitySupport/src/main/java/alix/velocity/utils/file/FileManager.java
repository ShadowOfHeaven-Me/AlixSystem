package alix.velocity.utils.file;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.commands.file.CommandsFileManager;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.security.email.EmailConfig;
import alix.common.data.settings.ServerSettingsManager;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.config.alix.AlixYamlConfig;
import alix.velocity.systems.packets.gui.menu.MenuConfig;

import java.util.concurrent.TimeUnit;

public final class FileManager {

    private static final boolean antiBotService = true;

    public static void loadFiles() {
        UserFileManager.init();
        UserTokensFileManager.init();
        EmailConfig.init();
        ServerSettingsManager.init();
        //Was previously never explicitly init()'d on Velocity (unlike Spigot's FileManager, which already
        //does both init() and save() for this one) - it still "worked" only because AllowListFileManager's
        //own static initializer runs the moment the class is first touched, e.g. from AntiVPN/GeoIPTracker's
        //own static initializers, but ONLY if those happen to run at all - with "anti-vpn: false" and
        //"max-total-accounts" at/under 0, neither of those classes was ever touched, so "allow-list.txt"
        //silently never got created, exactly like the email-templates bug fixed earlier. Calling init() here
        //unconditionally (init() itself is a no-op method - the real work already happened in the static
        //initializer that this call forces to run) makes file creation eager regardless of config, while the
        //actual bypass-list *behavior* (whether it's consulted at all) stays exactly as config-gated as before.
        AllowListFileManager.init();
        if (antiBotService) FireWallManager.init();

        AlixScheduler.repeatAsync(FileManager::onAsyncSave, 1, TimeUnit.MINUTES);
    }

    private static void onAsyncSave() {
        UserFileManager.onAsyncSave();
        UserTokensFileManager.save();
        //Also previously missing entirely on Velocity (present on Spigot) - meant that "/as bl/bypasslimit"
        //and "/as bl-r/bypasslimit-remove" changes were never actually persisted to disk here or on shutdown,
        //only kept in memory until the next restart silently discarded them.
        AllowListFileManager.save();
        if (antiBotService) FireWallManager.onAsyncSave();
    }

    public static void saveFiles() {
        UserFileManager.fastSave();
        UserTokensFileManager.save();
        AllowListFileManager.save();
        if (antiBotService) FireWallManager.fastSave();
    }

    /**
     * v1.5.2 - backs "/as reload" (AlixSystemCommand). Re-reads everything that CAN safely be re-read
     * from disk without a full proxy restart, in place, without touching anything already running
     * (connections, the anti-bot engine's live state, scheduled tasks, etc.) - see each called method's
     * own docblock for exactly what it does and does not cover.
     * <p>
     * Deliberately NOT included, because it genuinely cannot be done without a real restart:
     * <ul>
     *     <li>email-config.yml (Support\EmailConfig caches host/username/password/... into `final`
     *     fields at startup - there is no live instance to safely swap without risking an in-flight
     *     email send using half-old/half-new SMTP settings)</li>
     *     <li>command names/aliases actually registered with Velocity's Brigadier dispatcher (fixed at
     *     AlixSystemCommand#register() time - see CommandsFileManager#reload()'s docblock)</li>
     *     <li>anything else cached into a `final` field at startup rather than read live per-use -
     *     config.yml/database.yml values ARE re-read (see AlixYamlConfig#reloadAll()), but only code
     *     that queries them fresh each time it's needed will actually see the new value without a
     *     restart</li>
     * </ul>
     */
    public static void reloadFiles() {
        AlixYamlConfig.reloadAll();
        Messages.getFileInstance().loadExceptionless();
        ServerSettingsManager.reload();
        CommandsFileManager.reload();
        AllowListFileManager.reload();
        //Deliberately NOT calling FireWallManager.init() here - like every other init() in this
        //codebase (see loadFiles() above) it's a no-op placeholder whose real work already ran in a
        //static initializer at class-load time; calling it again would do nothing and just look like
        //it reloaded something it didn't. Its own config (config.yml) is still covered by
        //AlixYamlConfig.reloadAll() above for whichever of its checks read that config live.
        MenuConfig.reloadAll();
    }
}