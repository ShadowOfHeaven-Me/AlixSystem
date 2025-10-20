package alix.velocity;

import alix.common.MainClass;
import alix.common.logger.velocity.VelocityLoggerAdapter;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.floodgate.GeyserUtil;
import alix.common.utils.formatter.AlixFormatter;
import alix.loaders.classloader.LoaderBootstrap;
import alix.loaders.velocity.VelocityAlixMain;
import alix.velocity.server.AlixVelocityLimbo;
import alix.velocity.systems.commands.CommandManager;
import alix.velocity.systems.events.Events;
import alix.velocity.systems.packets.PacketEventsManager;
import alix.velocity.utils.AlixChannelInitInterceptor;
import alix.velocity.utils.AlixUtils;
import alix.velocity.utils.file.FileManager;
import alix.velocity.utils.user.UserManager;
import com.velocitypowered.proxy.VelocityServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@MainClass
public final class Main implements LoaderBootstrap {

    public static final VelocityAlixMain PLUGIN = VelocityAlixMain.instance;
    public static Main INSTANCE;
    private final VelocityServer server;
    private final Logger logger;
    private final Path dataDirectory;
    public GeyserUtil util;

    public Main(VelocityAlixMain plugin, Logger logger, Path dataDirectory) {
        this.server = (VelocityServer) plugin.getServer();
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        INSTANCE = this;
    }

    //Use the onLoad method with caution
    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        //LibbyManager.loadDependencies(PLUGIN, this.logger, this.dataDirectory, this.server.getPluginManager());
        this.util = new GeyserUtil(server.getPluginManager().getPlugin("floodgate").isPresent());
        //logInfo("floodgate present: " + this.util.isFloodgatePresent());
        //AlixServer.init();
        Messages.init();
        var pluginContainer = server.getPluginManager().getPlugin("alixsystem").orElseThrow(() -> new RuntimeException("Co do kurwy chuja"));

        PacketEventsManager.init(pluginContainer, server, this.logger, this.dataDirectory);
        AlixVelocityLimbo.init();

        PacketEventsManager.register();

        this.server.getEventManager().register(PLUGIN, new Events(this.util));
        AlixChannelInitInterceptor.initializeInterceptor(this.server);

        CommandManager.register(this.server);
        FileManager.loadFiles();
        AlixUtils.init();
        UserManager.init(this.server);
        //server.getBackendChannelInitializer();
        //VelocityServerConnection
    }

    @Override
    public void onDisable() {
        AlixScheduler.shutdown();
        FileManager.saveFiles();
    }

    public static void logInfo(String info) {
        VelocityLoggerAdapter.sendMessage(PLUGIN.getServer().getConsoleCommandSource(), AlixFormatter.colorize(info));
    }
}