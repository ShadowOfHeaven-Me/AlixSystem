package alix.loaders.bukkit;

import alix.common.CommonAlixMain;
import alix.common.logger.AlixLoggerProvider;
import alix.common.logger.LoggerAdapter;
import alix.common.update.FileUpdater;
import alix.pluginloader.JarInJarClassLoader;
import alix.pluginloader.LoaderBootstrap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class BukkitAlixMain extends JavaPlugin implements AlixLoggerProvider {

    public static BukkitAlixMain instance;
    private static final String JAR_NAME = "AlixSystem.jarinjar";
    private static final String BOOTSTRAP_CLASS = "shadow.Main";
    private final LoaderBootstrap plugin;
    private final JarInJarClassLoader loader;
    private final LoggerAdapter logger;

    public BukkitAlixMain() {
        instance = this;
        CommonAlixMain.loggerManager = this;

        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, JavaPlugin.class, this);
        this.logger = LoggerAdapter.createAdapter(super.getLogger());


        CommonAlixMain.plugin = this.plugin;

        saveDefaultConfig();

        FileUpdater.updateFiles();

/*        if (UpdateChecker.checkForUpdates(this)) {
            this.plugin = null;
        } else this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, JavaPlugin.class, this);*/
    }

    @Override
    public void onLoad() {
        this.plugin.onLoad();
    }

    @Override
    public void onEnable() {
        this.plugin.onEnable();
    }

    @Override
    public void onDisable() {
        try {
            this.plugin.onDisable();
        } finally {
            try {
                this.loader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String msg = "§cThis command (/" + label + ") has not been registered! Report this as a bug immediately!";
        if (sender instanceof Conversable) ((Conversable) sender).sendRawMessage(msg);
        else sender.sendMessage(msg);
        return false;
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return logger;
    }
}