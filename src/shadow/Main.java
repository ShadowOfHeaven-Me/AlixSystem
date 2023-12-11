package shadow;

import alix.common.scheduler.impl.AlixScheduler;
import alix.common.scheduler.runnables.AlixThread;
import alix.common.update.UpdateChecker;
import alix.common.utils.security.Hashing;
import alix.pluginloader.LoaderBootstrap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.login.Verifications;
import shadow.systems.login.autoin.PremiumAutoIn;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.systems.metrics.Metrics;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.FileManager;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.gui.builders.AnvilPasswordBuilder;
import shadow.utils.users.UserManager;
import shadow.utils.world.AlixWorld;

import java.util.logging.Level;

import static shadow.utils.main.AlixUtils.anvilPasswordGui;
import static shadow.utils.main.AlixUtils.requireCaptchaVerification;

public final class Main implements LoaderBootstrap {

    public static Thread mainServerThread;
    public static JavaPlugin plugin;
    public static YamlConfiguration config;
    public static PluginManager pm;
    private Metrics metrics;
    private boolean en = true;

    //UPDATE:
    //[+] Added 1.20.2 support
    //[+] Not logged in users will now also have an xp countdown
    //[+] Enhanced verification command processing speed
    //[+] Added colors to the console prefix
    //[+] Added custom join console logs, with the possibility of disabling them at "custom-join-log" in the config file
    //[*] Fixed the error thrown after disabling captcha
    //[*] Fixed players not being teleported to the captcha world when captcha was disabled
    //[*] Fixed console filters adding up after reloads
    //[*] The commands /captcha, /register, /login & /changepassword will no longer be visible in the console
    //[*] The PaperAixScheduler will now have it's one tick task delay removed in exchange for a fast synchronization
    //[*] "use-this-syntax" for chat formatting intervention will now be false by default
    //[*] "ping-before-join" will now be false by default
    //[*] Now whenever a user tries to create a command-written password with spaces, he will be told that spaces cannot be used for passwords, instead of just accepting the first inputted argument
    //[*] Halved the amount of thread assigned to the async scheduler
    //[*] Stabilized captcha regeneration logic
    //[*] Both pre-register lists will now be the size of 500 by default
    //[*] Undeprecated, redesigned and made true by default "check-compatibility", and renamed it to "check-server-compatibility"


    //todo: Add a custom data structure for unverified users
    //TODO: fix pin gui's location sounds bugs
    //TODO: add book captcha possibility in config

/*    #Defines whether the numerical digits generated on a captcha map item should be 'fancy'
            #Turning this on, however, will require the captcha length to be 8 or less (due to how big the digits are)
#It will also enlarge the required amount of calculations, that needs to be made on the captcha rendering
    fancy-digits: false*/

/*    #Defines the way the unregistered/not logged in players' actions will be restricted. Available: packet & teleport ('teleport' parameter is currently disabled)
            #packet - cancels all unnecessary client packets until the player logs in or leaves (could false some anticheats or result in errors on older versions of minecraft - should be tested beforehand, however it could potentially work as a safety measure against a bot attack)
#teleport - teleports the player to their starting position every 3 seconds (supports every version, however right now it's very buggy (the inventory can be modified before logging in) and definitely not recommended)
            login-restrict-base: packet*/

    //TODO: /blacklist, mute-ip (?), ban format customizable
    //TODO: ping check by Keep Alive packets or the CraftPlayer getPing method (?)
    //TODO: Check if delay that's zero or less can bug the scheduler (packet auto kick)

/*    #Defines the ban format
# \n - for a new line
# %sender% - the ban executor
# %reason% - reason of the ban
    ban-format: ""*/




/*#----------------------------------------------#
        #            PING CHECK (Experimental)         #
            #----------------------------------------------#

            #Defines whether captcha unverified players should be checked for ping before being able to start registering
    ping-check: true
            #Defines the maximum acceptable ping average - anything above that will make the player fail the ping check
    ping-threshold: 250
            #Defines the amount of ping checks sent in order to check the player's ping
    ping-checks: 5*/

    /**/


    public Main(JavaPlugin instance) {
        plugin = instance;
    }

    @Override
    public void onLoad() {
        //if (autoRestart()) return;
        logConsoleInfo("Successfully loaded the plugin from an external loader.");
        config = (YamlConfiguration) plugin.getConfig();
        pm = Bukkit.getPluginManager();
        AlixHandler.kickAll("Reload");
        //AlixTranslator.init();
    }

    @Override
    public void onEnable() {//TODO: Player GUI in /js commands (texture not working & items can be picked up)
        config.options().copyDefaults(true);
        if (AlixWorld.preload()) logConsoleInfo("Successfully pre-loaded the captcha world");
        Hashing.init();//Making sure all the hashing algorithms exist by loading the Hashing class
        //AlixWorld.init();
        Captcha.pregenerate();
        FileManager.loadFiles();
        AlixHandler.initExecutors(pm);
        AlixCommandManager.init();
        //Dependencies.initAdditional();
        VerificationReminder.init();
        if (anvilPasswordGui) AnvilPasswordBuilder.init();
        AlixScheduler.sync(this::setUp);
        this.metrics = Metrics.createMetrics();
    }

    @Override
    public void onDisable() {//ChatColor.of(color)
        FileManager.saveFiles();
        Verifications.disable();
        UserManager.disable();
        //Captcha.unregister();
        AlixScheduler.shutdown();
        if (this.metrics != null) this.metrics.shutdown();
        AlixThread.shutdownAllAlixThreads();
        logConsoleInfo(en ? "AlixSystem has been disabled." : "AlixSystem zostało wyłączone.");
    }

    public static void disable() {
        pm.disablePlugin(plugin);
    }

    public static boolean isAccessible() {
        return plugin != null;
    }

    public static void logInfo(String info) {
        plugin.getLogger().info(info);
    }

    public static void logWarning(String warning) {
        plugin.getLogger().warning(warning);
    }

    public static void logError(String error) {
        plugin.getLogger().severe(error);
    }

    public static void debug(String info) {
        if (AlixUtils.isDebugEnabled) plugin.getLogger().log(Level.CONFIG, info);
    }

    private void setUp() {
        en = AlixUtils.isPluginLanguageEnglish;
        mainServerThread = Thread.currentThread();
        //AlixHandler.kickAll("Reload");
        UpdateChecker.checkForUpdates();
        PremiumAutoIn.checkForInit();
        CommandManager.register(plugin);
        if (requireCaptchaVerification) Captcha.sendInitMessage();
        PacketBlocker.init(); //load the class and send the init message
        //ConfigUpdater.checkForUpdates(config);
        ReflectionUtils.init();
        if (config.getBoolean("check-compatibility")) {
            //logConsoleInfo(en ? "Started checking for plugin compatibilities..." : "Rozpoczęto sprawdzanie kompatybilności pluginów...");
            //checkPlugins();
            if (Bukkit.getConnectionThrottle() > 500 && config.getBoolean("prevent-first-time-join")) {
                logWarning("The connection throttle in bukkit.yml settings is " + Bukkit.getConnectionThrottle()
                        + " with the recommended number being 500 or below. If you wish to have it overridden automatically, " +
                        "type /alixsystem connection-setup or /as c-s for short.");
            }
            //logConsoleInfo(en ? "Done!" : "Ukończono!");
        }
        logConsoleInfo(en ? "AlixSystem has been successfully enabled." : "AlixSystem zostało poprawnie włączone.");
    }

/*    private void checkPlugins() {
        StringBuilder unnecessaryPlugins = new StringBuilder();
        String[] unwantedPlugins = {"EssentialsX", "LoginSecurity", "AuthMe", "SimpleCommands"};
        for (String plugin : unwantedPlugins) {
            if (pm.isPluginEnabled(plugin)) {
                unnecessaryPlugins.append(plugin).append(',');
                //disablePlugin(plugin);
            }
        }
        String toText = unnecessaryPlugins.toString();
        if (!toText.isEmpty()) {
            final String substring = toText.substring(0, toText.length() - 2);
            logConsoleInfo(en ? "AlixSystem has deemed these plugins unnecessary: " + substring :
                    "AlixSystem uznało te pluginy za zbędne: " + substring);
        }
        boolean antiCheat = false;//, antiCrash = false;
        for (Plugin plugin : pm.getPlugins()) {
            String name = plugin.getName().toLowerCase();
            //if (name.contains("chat")) chatPluginFound = true;
            if (name.contains("anticheat")) antiCheat = true;
            //if (name.contains("anticrash")) antiCrash = true;
            switch (name) {
                case "vulcan":
                case "spartan":
                case "kauri":
                case "grimac":
                    antiCheat = true;
                    break;
                //case "packetlimiter":
*//*                case "exploitfixer":
                //case "titanium":
                    antiCrash = true;
                    break;*//*
            }
        }

        *//*if (!antiCrash)
            logConsoleWarning(en ? "No AntiCrash enlisted in AlixSystem's library was found. Recommending Titanium (AntiCrash). Download link: https://www.spigotmc.org/resources/titanium-anticrash.102161/"
                    : "Nie znaleziono żadnego AntyCrasha wpisanego w bibliotekę AlixSystem, rekomenduję Titanium (AntiCrash). Link do pobrania: https://www.spigotmc.org/resources/titanium-anticrash.102161/");
*//*
        if (!antiCheat)
            logConsoleWarning(en ? "No AntiCheat enlisted in AlixSystem's library was found. Recommending either GrimAC or Kauri. " +
                    "Full lists of anticheats can be found here: https://www.spigotmc.org/wiki/anti-cheat-list-bukkit-and-spigot-1-17-part-1/ " +
                    "or here https://www.spigotmc.org/wiki/anti-cheat-list-bukkit-and-spigot-1-19-x/" :
                    "Nie znaleziono żadnego AntyCheatu wpisanego w bibliotekę AlixSystem, rekomenduję GrimAC lub Kauri. " +
                            "Listy możliwych opcji można znależć tu: https://www.spigotmc.org/wiki/anti-cheat-list-bukkit-and-spigot-1-17-part-1/ " +
                            "lub tu https://www.spigotmc.org/wiki/anti-cheat-list-bukkit-and-spigot-1-19-x/");
        //if (!chatPluginFound && !en)
        //logConsoleInfo("Nie znaleziono pluginu na regulację chatu, rekomenduję G5ChatMod autorstwa kubyg5. Link do pobrania: https://kubag5.pl/G5ChatMod");
        if (ReflectionUtils.protocolVersion && pm.isPluginEnabled("WorldEdit") && !pm.isPluginEnabled("FastAsyncWorldEdit")) {
            logConsoleWarning(en ? "Recommending WorldEdit alternative, FAWE (FastAsyncWorldEdit), for better performance. Download link: " +
                    "https://www.spigotmc.org/resources/fastasyncworldedit.13932/" :
                    "Rekomenduję zamiennik WorldEdita, FAWE (FastAsyncWorldEdit), dla mniejszego zużycia procesora. Link do pobrania: " +
                            "https://www.spigotmc.org/resources/fastasyncworldedit.13932/");
        }
    }*/


    /*    public void disablePlugin(String name) {
        Plugin plugin = pm.getPlugin(name);
        pm.disablePlugin(plugin);
        logConsoleInfo(en ? "Plugin " + name + " was disabled!" : "Plugin " + name + " został wyłączony!");
    }

    private boolean autoRestart() {
        boolean access = isAccessible();
        if (access) {
            logConsoleWarning(en ? "Plugin loaded in twice without disabling!" : "Plugin załadował się dwukrotnie bez wyłączania!");
            restart();
        }
        return access;
    }

    public void restart() {
        logConsoleInfo(en ? "Initiating plugin restart!" : "Rozpoczęto restart pluginu!");
        onDisable();
        onEnable();
    }*/

    public void logConsoleInfo(String info) {
        plugin.getLogger().info(info);
        //System.out.println(info);
        //MessageUtils.toStdout(
    }

    public void logConsoleWarning(String warning) {
        plugin.getLogger().warning(warning);
    }
}