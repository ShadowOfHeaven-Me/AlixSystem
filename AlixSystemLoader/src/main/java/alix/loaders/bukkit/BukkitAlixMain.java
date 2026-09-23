package alix.loaders.bukkit;

import alix.common.AlixCommonMain;
import alix.common.AlixMain;
import alix.common.MainClass;
import alix.common.logger.AlixLoggerProvider;
import alix.common.logger.LoggerAdapter;
import alix.common.logger.plugin.BukkitAlixLogger;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.file.update.FileUpdater;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import alix.loaders.classloader.LoaderBootstrap;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Logger;

@MainClass
public final class BukkitAlixMain extends JavaPlugin implements AlixLoggerProvider, AlixMain {

    public static BukkitAlixMain instance;
    public static Thread mainServerThread;
    //private static final String JAR_NAME = "AlixSystem.jar";
    private static final String BOOTSTRAP_CLASS = "shadow.Main";
    private final LoaderBootstrap bootstrap;
    //private final JarInJarClassLoader loader;
    private final LoggerAdapter loggerAdapter;
    private final Logger logger;
    //private final AlixPluginLogger alixLogger;

    @SneakyThrows
    public BukkitAlixMain() {
        AlixException.init();
        AlixError.init();
        instance = this;
        //CommonAlixMain.loggerManager = this;
        this.logger = new BukkitAlixLogger();
        this.loggerAdapter = LoggerAdapter.createAdapter(this.logger);
        //this.logger.info("LOADER BUKKIT ALIX.Main: " + BukkitAlixMain.class.getClassLoader());

        //this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        //this.bootstrap = (LoaderBootstrap) loader.instantiatePlugin(BOOTSTRAP_CLASS, BukkitAlixMain.class, this);
        this.bootstrap = (LoaderBootstrap) Class.forName(BOOTSTRAP_CLASS).getConstructor(BukkitAlixMain.class).newInstance(this);

        saveDefaultConfig();
        FileUpdater.updateFiles();

/*        if (UpdateChecker.checkForUpdates(this)) {
            this.plugin = null;
        } else this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, JavaPlugin.class, this);*/
    }

/*    private void replaceLogger() {
        Field f2 = null;
        for (Field f : JavaPlugin.class.getDeclaredFields()) {
            f.setAccessible(true);
            if (Logger.class.isAssignableFrom(f.getType())) {
                f2 = f;
                break;
            }
        }
        assert f2 != null : "No Logger variable found at JavaPlugin.class - " + Arrays.toString(JavaPlugin.class.getDeclaredFields());
        try {
            f2.set(this, new AlixPluginLogger());
        } catch (IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
        *//*switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT: {
                try {
                    Field f2 = null;
                    ArrayList<String> debug = new ArrayList<>();
                    for (Field f : PluginLogger.class.getDeclaredFields()) {
                        f.setAccessible(true);
                        debug.add(f.getName() + " " + f.getType());
                        if (f.getType() == String.class) {
                            f2 = f;
                            break;
                        }
                    }
                    if (f2 == null) throw new ExceptionInInitializerError("Broken. Debug: " + debug.toString());

                    f2.set(super.getLogger(), "\u001B[1;31m" + "[AlixSystem]" + "\u001B[0m ");
                } catch (Exception e) {
                    throw new ExceptionInInitializerError(e);
                }
                break;
            }

            case PAPER: {
                *//**//*getLogger().info(this.getLogger().getClass().getSimpleName()
                        + " " + Arrays.toString(this.getLogger().getClass().getDeclaredFields())
                        + " " + Arrays.toString(this.getLogger().getClass().getDeclaredMethods())
                        + " " + Arrays.toString(this.getClass().getDeclaredMethods()));*//**//*
                break;
            }

            default:
                throw new AssertionError("The fuq: " + ServerEnvironment.getEnvironment());
        }*//*
    }*/

    @Override
    public void onLoad() {
        mainServerThread = Thread.currentThread();
        //CommonReflection.set(CommonReflection.getFieldAccessibleByType(JavaPlugin.class, Logger.class), this, this.logger);
        this.bootstrap.onLoad();
    }

    @Override
    public void onEnable() {
        this.bootstrap.onEnable();
    }

    @Override
    public void onDisable() {
        this.bootstrap.onDisable();
        /*try {
            this.bootstrap.onDisable();
        } finally {
            this.loader.close();
        }*/
    }

    @NotNull
    public Logger getAlixLogger() {
        return logger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§cThis command (/" + label + ") has not been registered! Report this as a bug immediately!");
        return false;
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return this.loggerAdapter;
    }

    @Override
    public Path getDataFolderPath() {
        return getDataFolder().toPath();
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

    //loads the common module (not all classes tho)
    public ClassLoader getClassLoader0() {
        return super.getClassLoader();
    }

    //Loads the spigot module
    /*@RemotelyInvoked
    public JarInJarClassLoader getJarInJarLoader() {
        return this.loader;
    }*/

    private static final class ParamImpl implements Params {

        //supported message-language codes; every one except DEFAULT_LANGUAGE maps to a bundled, read-only
        //translation under the "langs" resource folder (e.g. "cs" -> langs/cs.txt - see messagesFileName()
        //and FileUpdater's SPIGOT/PAPER branch for why these are never merge-preserved the way messages.txt
        //is). Deliberately separate from the older 'isPluginLanguageEnglish' en/pl switch a handful of
        //classes still use directly (AlixUtils#isPluginLanguageEnglish and friends) - that one predates this
        //mechanism and isn't affected by it either way; "pl" simply isn't a valid choice for this newer,
        //messages.txt-routed system, and falls back to English (messages.txt) here the same as any other
        //unsupported value would.
        private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "cs");
        private static final String DEFAULT_LANGUAGE = "en";
        //The default/canonical messages file - unlike a "langs/<code>.txt" bundled translation, this one is
        //a normal, merge-updated config file (like config.yml): fully customizable, and every edit survives
        //a plugin update.
        private static final String DEFAULT_MESSAGES_FILE = "messages.txt";

        @Override
        public String messagesFileName() {
            String language = ConfigProvider.config.getString("language", DEFAULT_LANGUAGE).toLowerCase();
            if (!SUPPORTED_LANGUAGES.contains(language)) {
                //Deliberately not a warning for "pl" specifically - that's a real, supported value for the
                //older isPluginLanguageEnglish switch, just not for this one, so logging a warning here would
                //incorrectly suggest something is misconfigured for an operator who only ever intended to
                //opt into the older system.
                if (!language.equals("pl"))
                    AlixCommonMain.logWarning("Unsupported 'language' value '" + language + "' in config.yml, falling back to '" + DEFAULT_LANGUAGE + "'. Supported values: " + SUPPORTED_LANGUAGES);
                language = DEFAULT_LANGUAGE;
            }
            return language.equals(DEFAULT_LANGUAGE) ? DEFAULT_MESSAGES_FILE : "langs/" + language + ".txt";
        }

        @Override
        public String referenceMessagesFileName() {
            return DEFAULT_MESSAGES_FILE;
        }

        private ParamImpl() {
        }
    }

    public void setEnabledStatus(boolean on) {
        this.setEnabled(on);
    }

    /*    private static final class AlixPluginLogger extends PluginLogger {

        public AlixPluginLogger() {
            super(BukkitAlixMain.instance);
        }

        @Override
        public final void log(@NotNull LogRecord logRecord) {
            super.log(logRecord);
        }
    }*/
}