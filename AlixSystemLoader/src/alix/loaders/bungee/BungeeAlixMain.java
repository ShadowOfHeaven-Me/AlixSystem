package alix.loaders.bungee;

import alix.common.logger.AlixLoggerProvider;
import alix.common.logger.LoggerAdapter;
import alix.common.utils.file.update.FileUpdater;
import alix.loaders.classloader.JarInJarClassLoader;
import alix.loaders.classloader.LoaderBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeAlixMain extends Plugin implements AlixLoggerProvider {

    public static BungeeAlixMain instance;
    private static final String JAR_NAME = "AlixSystemBungee.jarinjar";
    private static final String BOOTSTRAP_CLASS = "alix.bungee.Main";
    private final LoaderBootstrap plugin;
    private final JarInJarClassLoader loader;
    private final LoggerAdapter logger;

    public BungeeAlixMain() {
        instance = this;
        //CommonAlixMain.loggerManager = this;

        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.plugin = (LoaderBootstrap) loader.instantiatePlugin(BOOTSTRAP_CLASS, Plugin.class, this);
        this.logger = LoggerAdapter.createAdapter(super.getLogger());

        //CommonAlixMain.bootstrap = this.plugin;

        FileUpdater.updateFiles();
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
            this.loader.close();
        }
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return logger;
    }
}