package alix.velocity;

import alix.loaders.velocity.VelocityAlixMain;
import alix.pluginloader.LoaderBootstrap;
import alix.velocity.systems.autoin.PremiumAutoIn;
import alix.velocity.systems.events.Events;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import net.kyori.adventure.audience.ForwardingAudience;

public class Main implements LoaderBootstrap {

    public static VelocityAlixMain instance;
    public static PluginManager pm;
    private final VelocityServer server;

    public Main(VelocityAlixMain plugin) {
        this.server = (VelocityServer) plugin.getServer();
        instance = plugin;
        pm = this.server.getPluginManager();
    }

    //Use the onLoad method with caution
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        PremiumAutoIn.checkForInit();
        this.server.getEventManager().register(instance, new Events());
        //server.getBackendChannelInitializer();
        //VelocityServerConnection
    }

    @Override
    public void onDisable() {

    }
}