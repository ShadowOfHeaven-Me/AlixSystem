package shadow;

import alix.common.data.security.password.hashing.Hashing;
import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.runnables.AlixThread;
import alix.common.utils.file.update.UpdateChecker;
import alix.common.utils.formatter.AlixFormatter;
import alix.loaders.bukkit.BukkitAlixMain;
import alix.loaders.classloader.LoaderBootstrap;
import com.github.retrooper.packetevents.PacketEvents;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.executors.PreStartUpExecutors;
import shadow.systems.executors.packetevents.PacketEventsManager;
import shadow.systems.gui.impl.IpAutoLoginGUI;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.systems.metrics.Metrics;
import shadow.systems.netty.AlixInterceptor;
import shadow.systems.virtualization.manager.UserSemiVirtualization;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.FileManager;
import shadow.utils.misc.ReflectionUtils;
import shadow.utils.misc.effect.PotionEffectHandler;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.gui.builders.BukkitAnvilPasswordBuilder;
import shadow.utils.world.AlixWorld;

import java.util.logging.Level;

import static shadow.utils.main.AlixUtils.requireCaptchaVerification;

public final class Main implements LoaderBootstrap {

    public static Thread mainServerThread;
    public static BukkitAlixMain plugin;
    public static YamlConfiguration config;
    public static final PluginManager pm = Bukkit.getPluginManager();
    private Metrics metrics;
    private boolean en = true;

    //UPDATE:
    //* Fixed a rare concurrency exception

    //*

    //Note:
    //to do: Fix players trying to enter on different versions experiencing issues when they decide to use the GUIs (Currently disabled)

    //todo: Add a custom data structure for unverified users
    //TODO: add book captcha possibility in config

    //TODO: /blacklist, mute-ip (?), ban format customizable
    //TODO: ping check by Keep Alive packets or the CraftPlayer getPing method (?)
    //TODO: Check if delay that's zero or less can bug the scheduler (packet auto kick)

/*    #Defines the ban format
# \n - for a new line
# %sender% - the ban executor
# %reason% - reason of the ban
    ban-format: ""*/


/*#If set to true, Alix will immediately forcibly close connections and have the player see the automatic "Connection Refused" message.
#If set to false, the "Your IP has been firewalled" message will be shown after waiting for login packets (a bit less optimized).
    fast-raw-firewall: true*/

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


    public Main(BukkitAlixMain instance) {
        plugin = instance;
    }

    //Don't use alix scheduler on load, cuz errors on paper
    @SneakyThrows
    @Override
    public void onLoad() {
        //ServerLoadEvent
        //if (autoRestart()) return;
        logConsoleInfo("Successfully loaded the plugin from an external loader.");
        PacketEventsManager.onLoad();

        //logInfo("SEX " + Bukkit.getIp() + " " + Bukkit.getPort() + " LOCAL " + InetAddress.getLocalHost());

        //Main.logError("LOADERRRR " + PacketEvents.class.getClassLoader());
        config = (YamlConfiguration) plugin.getConfig();
        this.metrics = Metrics.createMetrics();
        //logError(FigletFont.convertOneLine("SEX12"));

        kickAll("Reload");

        //PacketType.Play.Server.PLUGIN_MESSAGE
    }

    @Override
    public void onEnable() {
        //AlixScheduler.repeatAsync(() -> logInfo("Size: " + Bukkit.getOnlinePlayers().size() + " Ver: " + UserManager.userCount()), 1L, TimeUnit.SECONDS);
        //AlixBungee.init();
        //ParticleRenderer3d.render();
        PreStartUpExecutors preStartUpExecutors = new PreStartUpExecutors();
        AlixInterceptor.init();
        pm.registerEvents(preStartUpExecutors, plugin);
        config.options().copyDefaults(true);
        mainServerThread = Thread.currentThread();
        //PacketCaptureManager.init();
        Hashing.init();//Making sure all the hashing algorithms exist by loading the Hashing class
        VerificationReminder.init();
        BukkitAnvilPasswordBuilder.init();
        MethodProvider.init();
        IpAutoLoginGUI.init();
        AlixCommandManager.init();
        Dependencies.initAdditional();
        if (AlixWorld.preload()) logConsoleInfo("Successfully pre-loaded the captcha world");
        AlixScheduler.sync(() -> AlixScheduler.async(() -> this.setUp(preStartUpExecutors)));//sync in order to have the message sent after start-up, and async to not cause any slowdowns on the main thread
        if (Bukkit.getServer().getOnlineMode())
            AlixScheduler.sync(() -> Main.logError("Online mode is enabled! Alix is now mainly an offline mode plugin! Bear that in mind!"));
        UserSemiVirtualization.init();
        //AlixScheduler.sync(UserVirtualization::init);
    }

    //TODO: Fix constant buffers OOM with reloads
    @Override
    public void onDisable() {//ChatColor.of(color)
        //logConsoleInfo("Saving files...");
        //UserVirtualization.RETURN_ORIGINAL_PLAYER_LIST.run();
        FileManager.saveFiles();
        //if (AlixInterceptor.fireWallType == FireWallType.FAST_UNSAFE_NIO) AlixFastUnsafeNIO.unregister();
        UserSemiVirtualization.RETURN_ORIGINAL_SETUP.run();
        Captcha.cleanUp();
        //logConsoleInfo("Saved!");
        //Verifications.disable();
        //UserManager.disable();
        //Captcha.unregister();
        AlixScheduler.shutdown();
        if (this.metrics != null) this.metrics.shutdown();
        AlixThread.shutdownAllAlixThreads();
        AlixInterceptor.onDisable();
        PacketEvents.getAPI().terminate();

        //if (preStartUpExecutors != null) HandlerList.unregisterAll(preStartUpExecutors);
        //if (ServerEnvironment.getEnvironment() == ServerEnvironment.PAPER) PaperAccess.unregisterChannelListener();
        logConsoleInfo(en ? "AlixSystem has been disabled." : "AlixSystem zostało wyłączone.");
    }

    //Done this funky way because of class visibility issues, due to different ClassLoaders (fixed it ;])
/*    @RemotelyInvoked
    public LoginType getConfigLoginType() {
        return LoginType.from(config.getString("password-type").toUpperCase(), true, PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_14));
    }*/

/*    public static void disable() {
        pm.disablePlugin(plugin);
    }

    public static boolean isAccessible() {
        return plugin != null;
    }*/

    public static void debug(String debug) {
        logError("[DEBUG] " + debug);
    }

    public static void logInfo(String info) {
        plugin.getAlixLogger().info(info);
    }

    public static void logWarning(String warning) {
        plugin.getAlixLogger().warning(warning);
    }

    public static void logError(String error) {
        plugin.getAlixLogger().severe(error);
    }

    public static void logDebug(String info) {
        if (AlixUtils.isDebugEnabled) plugin.getAlixLogger().log(Level.CONFIG, info);
    }

    private void setUp(PreStartUpExecutors preStartUpExecutors) {
        en = AlixUtils.isPluginLanguageEnglish;
        AlixHandler.updateConsoleFilter();
        CommandManager.register();
        AlixHandler.initExecutors(pm);
        //AlixHandler.kickAll("Reload");
        //PacketEventsManager.onLoad();
        PacketEventsManager.onEnable();
        ReflectionUtils.replaceBansToConcurrent();
        UpdateChecker.checkForUpdates();
        //AlixScheduler.async(UpdateChecker::checkForUpdates);
        //logError("BEFORE CAPTCHA LOAD");
        Captcha.pregenerate(); //will not pregenerate the captcha itself if disabled, but needs to be invoked for the BufferedPackets values to pregenerate
        //logError("AFTER CAPTCHA LOAD");
        PotionEffectHandler.init();
        if (requireCaptchaVerification) Captcha.sendInitMessage();
        PacketBlocker.init(); //load the class
        //PacketInterceptor.init();//send the init message
        //ConfigUpdater.checkForUpdates(config);
        /*if (config.getBoolean("check-server-compatibility")) {
            //logConsoleInfo(en ? "Started checking for plugin compatibilities..." : "Rozpoczęto sprawdzanie kompatybilności pluginów...");
            //checkPlugins();
            *//*if (Bukkit.getConnectionThrottle() > 500 && config.getBoolean("prevent-first-time-join")) {
                logWarning("");
                logWarning("The connection throttle in bukkit.yml settings is " + Bukkit.getConnectionThrottle() + ",");
                logWarning("with the recommended number being 500 or below. The reason for that is that it can annoying for");
                logWarning("unregistered players when combined with Alix's 'prevent-first-time-join' safety measure. It's highly recommended to have");
                logWarning("it overridden automatically with /alixsystem connection-setup or /as c-s for short.");
                logWarning("");
            }*//*
            //logConsoleInfo(en ? "Done!" : "Ukończono!");
        }*/
        // String mode = PacketBlocker.serverboundNameVersion ? "ASYNC" : "SYNC";
        //logConsoleInfo(en ? "Booted in the mode: " + mode : "AlixSystem zostało uruchomione w trybie: " + mode);

        if (pm.getPlugin("FastLogin") != null)
            logWarning("As of version 3.5.0, Alix no longer needs FastLogin! Premium verification has been built into Alix!");

        //PcapManager.init();

        FileManager.loadFiles();//load all the classes before enabling the ability to join
        HandlerList.unregisterAll(preStartUpExecutors);
        logConsoleInfo(en ? "AlixSystem has been successfully enabled." : "AlixSystem zostało poprawnie włączone.");
    }

    public void logConsoleInfo(String info) {
        plugin.getAlixLogger().info(info);
        //System.out.println(info);
        //MessageUtils.toStdout(
    }

    private void kickAll(String reason) {
        String kickMessage = AlixFormatter.appendPrefix(reason);
        for (Player p : Bukkit.getOnlinePlayers()) p.kickPlayer(kickMessage);
    }

/*    public void logConsoleWarning(String warning) {
        plugin.getLogger().warning(warning);
    }*/
}