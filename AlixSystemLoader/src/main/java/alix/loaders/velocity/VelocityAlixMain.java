package alix.loaders.velocity;

import alix.common.AlixCommonMain;
import alix.common.AlixMain;
import alix.common.MainClass;
import alix.common.logger.AlixLoggerProvider;
import alix.common.logger.LoggerAdapter;
import alix.common.logger.velocity.VelocityLoggerAdapter;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.file.update.FileUpdater;
import alix.common.utils.other.throwable.AlixException;
import alix.loaders.classloader.LoaderBootstrap;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.natives.compression.JavaVelocityCompressor;
import com.velocitypowered.natives.util.Natives;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Date;

@MainClass
@Plugin(id = "alixsystem", name = "AlixSystem", version = "1.5.1 (DEV-1)", description = "AntiBot & Login System", url = "https://builtbybit.com/resources/alixvelocity.61304/",
        authors = "ShadowOfHeaven", dependencies = {@Dependency(id = "floodgate", optional = true), @Dependency(id = "geyser", optional = true)})
public final class VelocityAlixMain implements AlixLoggerProvider, AlixMain {

    public static VelocityAlixMain instance;
    //private static final String JAR_NAME = "AlixSystemVelocity.jarinjar";
    private static final String BOOTSTRAP_CLASS = "alix.velocity.Main";
    private final ProxyServer server;
    private final Logger logger;
    private final LoggerAdapter loggerAdapter;
    private final Path dataDirectory;
    //private final JarInJarClassLoader loader;
    private final LoaderBootstrap bootstrap;
    //private final YamlConfiguration config;

    @SneakyThrows
    @Inject
    public VelocityAlixMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        long until = 1788817349480L + 7 * 86400 * 1000L;
        if (System.currentTimeMillis() > until)
            throw new AlixException("Trial ended!");

        instance = this;
        this.server = server;
        this.logger = logger;
        this.loggerAdapter = new VelocityLoggerAdapter(server); //LoggerAdapter.createAdapter(logger);
        this.dataDirectory = dataDirectory;
        dataDirectory.toFile().mkdir();

        //new File(dataDirectory.toAbsolutePath().toString()).mkdir();

        //File f = AlixFileManager.createPluginFile("config.yml", AlixFileManager.FileType.CONFIG);
        //this.config = YamlConfiguration.loadConfiguration(f);

        //CommonAlixMain.loggerManager = this;

        //this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        //this.bootstrap = (LoaderBootstrap) loader.instantiatePlugin(BOOTSTRAP_CLASS, VelocityAlixMain.class, this);
        this.bootstrap = (LoaderBootstrap) Class.forName(BOOTSTRAP_CLASS, true, this.getClass().getClassLoader())
                .getConstructor(VelocityAlixMain.class, Logger.class, Path.class)
                .newInstance(this, logger, dataDirectory);
        //this.loader.close();
        //CommonAlixMain.bootstrap = this.plugin;

        FileUpdater.updateFiles();
        logger.info("Trial finishes at: {}", AlixCommonUtils.getFormattedDate(new Date(until)));
        //this.bootstrap.onLoad();
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        this.bootstrap.onEnable();
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        this.bootstrap.onDisable();
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return loggerAdapter;
    }

    /*public Logger getLogger() {
        return logger;
    }*/

    public static boolean nativePreferDirectBufs() {
        //JavaVelocityCipher requires heap
        //NativeVelocityCipher requires direct
        return Natives.compress.get() != JavaVelocityCompressor.FACTORY; //Natives.cipher.get() != JavaVelocityCipher.FACTORY;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public Path getDataFolderPath() {
        return dataDirectory;
    }

    @Override
    public LoaderBootstrap getBootstrap() {
        return bootstrap;
    }

    private final ParamImpl params = new ParamImpl();

    @Override
    public Params getEngineParams() {
        return this.params;
    }

    private static final class ParamImpl implements Params {

        //supported message-language codes; every one except DEFAULT_LANGUAGE maps to a bundled, read-only
        //translation under the "langs" resource folder (e.g. "cs" -> langs/cs.properties - see
        //messagesFileName() and FileUpdater's VELOCITY branch for why these are never merge-preserved the
        //way messages.properties is)
        private static final java.util.Set<String> SUPPORTED_LANGUAGES = java.util.Set.of("en", "cs");
        private static final String DEFAULT_LANGUAGE = "en";
        //The default/canonical messages file - unlike a "langs/<code>.properties" bundled translation, this
        //one is a normal, merge-updated config file (like config.yml): fully customizable, and every edit
        //survives a plugin update. Kept at the top level (not under "langs/") for backwards compatibility -
        //this is the exact file name/role this plugin has always used for "language: en", predating the
        //"langs/" folder entirely, so any pre-existing customization of it keeps working unchanged.
        private static final String DEFAULT_MESSAGES_FILE = "messages.properties";

        @Override
        public String messagesFileName() {
            String language = ConfigProvider.config.getString("language", DEFAULT_LANGUAGE).toLowerCase();
            if (!SUPPORTED_LANGUAGES.contains(language)) {
                AlixCommonMain.logWarning("Unsupported 'language' value '" + language + "' in config.yml, falling back to '" + DEFAULT_LANGUAGE + "'. Supported values: " + SUPPORTED_LANGUAGES);
                language = DEFAULT_LANGUAGE;
            }
            return language.equals(DEFAULT_LANGUAGE) ? DEFAULT_MESSAGES_FILE : "langs/" + language + ".properties";
        }

        @Override
        public char messagesSeparator() {
            return ':';
        }

        @Override
        public String referenceMessagesFileName() {
            return DEFAULT_MESSAGES_FILE;
        }

        private ParamImpl() {
        }
    }

    /*public YamlConfiguration getConfig() {
        return config;
    }*/
}