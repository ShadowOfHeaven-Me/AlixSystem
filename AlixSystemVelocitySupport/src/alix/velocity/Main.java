package alix.velocity;

import alix.common.MainClass;
import alix.loaders.classloader.LoaderBootstrap;
import alix.loaders.velocity.VelocityAlixMain;
import alix.velocity.systems.events.Events;
import alix.velocity.utils.AlixChannelInitInterceptor;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.proxy.VelocityServer;

@MainClass
public final class Main implements LoaderBootstrap {

    public static final VelocityAlixMain instance = VelocityAlixMain.instance;
    public static PluginManager pm;
    private final VelocityServer server;

    public Main(VelocityAlixMain plugin) {
        this.server = (VelocityServer) plugin.getServer();
        pm = this.server.getPluginManager();
    }

    //Use the onLoad method with caution
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        //AlixServer.init();
        this.server.getEventManager().register(instance, new Events());
        AlixChannelInitInterceptor.initializeInterceptor(this.server);
        //server.getBackendChannelInitializer();
        //VelocityServerConnection
    }

    @Override
    public void onDisable() {

    }
}