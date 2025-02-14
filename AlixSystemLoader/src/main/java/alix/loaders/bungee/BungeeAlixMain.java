package alix.loaders.bungee;

import alix.common.AlixMain;
import alix.common.MainClass;
import alix.common.logger.AlixLoggerProvider;
import alix.common.logger.LoggerAdapter;
import alix.common.utils.file.update.FileUpdater;
import alix.loaders.classloader.JarInJarClassLoader;
import alix.loaders.classloader.LoaderBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

import java.nio.file.Path;

@MainClass
public final class BungeeAlixMain extends Plugin implements AlixLoggerProvider, AlixMain {

    public static BungeeAlixMain instance;
    private static final String JAR_NAME = "AlixSystemBungee.jarinjar";
    private static final String BOOTSTRAP_CLASS = "alix.bungee.Main";
    private final LoaderBootstrap bootstrap;
    private final JarInJarClassLoader loader;
    private final LoggerAdapter logger;

    public BungeeAlixMain() {
        instance = this;
        //CommonAlixMain.loggerManager = this;

        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.bootstrap = (LoaderBootstrap) loader.instantiatePlugin(BOOTSTRAP_CLASS, Plugin.class, this);
        this.logger = LoggerAdapter.createAdapter(super.getLogger());

        //CommonAlixMain.bootstrap = this.plugin;

        FileUpdater.updateFiles();
    }

    @Override
    public void onLoad() {
        this.bootstrap.onLoad();
    }

    @Override
    public void onEnable() {
        this.bootstrap.onEnable();
    }

    @Override
    public void onDisable() {
        try {
            this.bootstrap.onDisable();
        } finally {
            this.loader.close();
        }
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return logger;
    }

    @Override
    public Path getDataFolderPath() {
        return super.getDataFolder().toPath();
    }

    @Override
    public LoaderBootstrap getBootstrap() {
        return this.bootstrap;
    }

    private final ParamImpl params = new ParamImpl();

    @Override
    public Params getEngineParams() {
        return this.params;
    }

    private static final class ParamImpl implements Params {

        @Override
        public String messagesFileName() {
            return "messages.txt";
        }

        private ParamImpl() {
        }
    }
}