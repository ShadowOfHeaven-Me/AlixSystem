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

    public Main(BukkitAlixMain instance) {
        plugin = instance;
    }

    //Don't use alix scheduler on load, cuz errors on paper
    @SneakyThrows
    @Override
    public void onLoad() {
        logConsoleInfo("Successfully loaded the plugin from an external loader.");
        PacketEventsManager.onLoad();
        config = (YamlConfiguration) plugin.getConfig();
        this.metrics = Metrics.createMetrics();
        kickAll("Reload");
    }

    @Override
    public void onEnable() {
        PreStartUpExecutors preStartUpExecutors = new PreStartUpExecutors();
        AlixInterceptor.init();
        pm.registerEvents(preStartUpExecutors, plugin);
        config.options().copyDefaults(true);
        mainServerThread = Thread.currentThread();
        Hashing.init();//Making sure all the hashing algorithms exist by loading the Hashing class
        VerificationReminder.init();
        BukkitAnvilPasswordBuilder.init();
        MethodProvider.init();
        IpAutoLoginGUI.init();
        AlixCommandManager.init();
        UserSemiVirtualization.init();
        Dependencies.initAdditional();
        if (AlixWorld.preload()) logConsoleInfo("Successfully pre-loaded the verification world");
        AlixScheduler.sync(() -> AlixScheduler.async(() -> this.setUp(preStartUpExecutors)));//sync in order to have the message sent after start-up, and async to not cause any slowdowns on the main thread
        if (Bukkit.getServer().getOnlineMode())
            AlixScheduler.sync(() -> Main.logError("Online mode is enabled! Alix is now mainly an offline mode plugin! Bear that in mind!"));
    }

    @Override
    public void onDisable() {
        FileManager.saveFiles();
        UserSemiVirtualization.returnOriginalSetup();
        Captcha.cleanUp();
        AlixScheduler.shutdown();
        if (this.metrics != null) this.metrics.shutdown();
        AlixThread.shutdownAllAlixThreads();
        AlixInterceptor.onDisable();
        PacketEvents.getAPI().terminate();
        logConsoleInfo(en ? "AlixSystem has been disabled." : "AlixSystem zostało wyłączone.");
    }

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
        PacketEventsManager.onEnable();
        ReflectionUtils.replaceBansToConcurrent();
        UpdateChecker.checkForUpdates();
        Captcha.pregenerate(); //will not pregenerate the captcha itself if disabled, but needs to be invoked for the BufferedPackets values to pregenerate
        PotionEffectHandler.init();
        if (requireCaptchaVerification) Captcha.sendInitMessage();
        PacketBlocker.init(); //load the class
        if (pm.getPlugin("FastLogin") != null)
            logWarning("As of version 3.5.0, Alix no longer needs FastLogin! Premium verification has been built into it!");

        FileManager.loadFiles();//load all the classes before enabling the ability to join
        AlixHandler.initSynSaving();
        HandlerList.unregisterAll(preStartUpExecutors);
        logConsoleInfo(en ? "AlixSystem has been successfully enabled." : "AlixSystem zostało poprawnie włączone.");
    }

    public void logConsoleInfo(String info) {
        plugin.getAlixLogger().info(info);
    }

    private void kickAll(String reason) {
        String kickMessage = AlixFormatter.appendPrefix(reason);
        for (Player p : Bukkit.getOnlinePlayers()) p.kickPlayer(kickMessage);
    }
}