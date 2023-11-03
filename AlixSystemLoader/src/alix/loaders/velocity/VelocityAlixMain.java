package alix.loaders.velocity;

import alix.common.CommonAlixMain;
import alix.common.logger.AlixLoggerProvider;
import alix.common.logger.LoggerAdapter;
import alix.pluginloader.JarInJarClassLoader;
import alix.pluginloader.LoaderBootstrap;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "AlixSystem", name = "AlixSystem", version = "2.7.0", description = "AntiBot", url = "https://www.spigotmc.org/resources/alixsystem.109144/",
        authors = "ShadowOfHeaven", dependencies = @Dependency(id = "FastLogin", optional = true))
public class VelocityAlixMain implements AlixLoggerProvider {

    public static VelocityAlixMain instance;
    private static final String JAR_NAME = "AlixSystemVelocity.jarinjar";
    private static final String BOOTSTRAP_CLASS = "alix.velocity.Main";
    private final ProxyServer server;
    private final LoggerAdapter logger;
    private final Path dataDirectory;
    private final JarInJarClassLoader loader;
    private final LoaderBootstrap plugin;

    @Inject
    public VelocityAlixMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = LoggerAdapter.createAdapter(logger);
        this.dataDirectory = dataDirectory;

        instance = this;
        CommonAlixMain.loggerManager = this;

        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, VelocityAlixMain.class, this);

        CommonAlixMain.plugin = this.plugin;

        this.plugin.onLoad();
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        this.plugin.onEnable();
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return logger;
    }

    public final ProxyServer getServer() {
        return server;
    }
}