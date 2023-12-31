package alix.loaders.velocity;

import alix.common.AlixMain;
import alix.common.logger.AlixLoggerProvider;
import alix.common.logger.LoggerAdapter;
import alix.common.update.FileUpdater;
import alix.common.utils.file.FileManager;
import alix.pluginloader.JarInJarClassLoader;
import alix.pluginloader.LoaderBootstrap;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Plugin(id = "AlixSystem", name = "AlixSystem", version = "2.7.3", description = "AntiBot", url = "https://www.spigotmc.org/resources/alixsystem.109144/",
        authors = "ShadowOfHeaven", dependencies = @Dependency(id = "FastLogin", optional = true))
public final class VelocityAlixMain implements AlixLoggerProvider, AlixMain {

    public static VelocityAlixMain instance;
    private static final String JAR_NAME = "AlixSystemVelocity.jarinjar";
    private static final String BOOTSTRAP_CLASS = "alix.velocity.Main";
    private final ProxyServer server;
    private final LoggerAdapter logger;
    private final Path dataDirectory;
    private final JarInJarClassLoader loader;
    private final LoaderBootstrap bootstrap;
    private final YamlConfiguration config;

    @Inject
    public VelocityAlixMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        this.server = server;
        this.logger = LoggerAdapter.createAdapter(logger);
        this.dataDirectory = dataDirectory;
        File f = FileManager.createPluginFile("vconfig.yml");
        this.config = YamlConfiguration.loadConfiguration(f);

        //CommonAlixMain.loggerManager = this;

        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.bootstrap = loader.instantiatePlugin(BOOTSTRAP_CLASS, VelocityAlixMain.class, this);
        //CommonAlixMain.bootstrap = this.plugin;

        FileUpdater.updateFiles();
        this.bootstrap.onLoad();
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        this.bootstrap.onEnable();
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return logger;
    }

    public final ProxyServer getServer() {
        return server;
    }

    @Override
    public Path getDataFolderPath() {
        return dataDirectory;
    }

    @Override
    public LoaderBootstrap getBootstrap() {
        return bootstrap;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}