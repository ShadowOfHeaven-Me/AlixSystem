package alix.loaders.bukkit;

import alix.common.AlixMain;
import alix.common.MainClass;
import alix.common.logger.AlixLoggerProvider;
import alix.common.logger.LoggerAdapter;
import alix.common.reflection.CommonReflection;
import alix.common.utils.file.update.FileUpdater;
import alix.common.utils.other.annotation.RemotelyInvoked;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import alix.loaders.classloader.JarInJarClassLoader;
import alix.loaders.classloader.LoaderBootstrap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.logging.Logger;

@MainClass
public final class BukkitAlixMain extends JavaPlugin implements AlixLoggerProvider, AlixMain {

    public static BukkitAlixMain instance;
    private static final String JAR_NAME = "AlixSystem.jarinjar";
    private static final String BOOTSTRAP_CLASS = "shadow.Main";
    private final LoaderBootstrap bootstrap;
    private final JarInJarClassLoader loader;
    private final LoggerAdapter loggerAdapter;
    private final Logger logger;
    //private final AlixPluginLogger alixLogger;

    public BukkitAlixMain() {
        AlixException.init();
        AlixError.init();
        instance = this;
        //CommonAlixMain.loggerManager = this;
        this.logger = AlixLoggerProvider.createServerAdequateLogger(super.getLogger());
        this.loggerAdapter = LoggerAdapter.createAdapter(this.logger);

        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.bootstrap = (LoaderBootstrap) loader.instantiatePlugin(BOOTSTRAP_CLASS, BukkitAlixMain.class, this);
        //this.alixLogger = new AlixPluginLogger();
        //CommonAlixMain.bootstrap = this.plugin;

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
        CommonReflection.set(CommonReflection.getFieldAccessibleByType(JavaPlugin.class, Logger.class), this, this.logger);
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
    @RemotelyInvoked
    public JarInJarClassLoader getJarInJarLoader() {
        return this.loader;
    }

    private static final class ParamImpl implements Params {

        @Override
        public String messagesFileName() {
            return "messages.txt";
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