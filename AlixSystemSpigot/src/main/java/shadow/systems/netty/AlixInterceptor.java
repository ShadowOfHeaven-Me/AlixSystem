package shadow.systems.netty;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.adaptive.ConnectionVerdict;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.epoll.AlixEpollConnection;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.FireWallType;
import alix.common.antibot.firewall.ataraxia.AlixAtaraxia;
import alix.common.environment.ServerEnvironment;
import alix.common.utils.AlixCommonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.unix.AlixFastUnsafeEpoll;
import io.papermc.paper.configuration.GlobalConfiguration;
import shadow.Main;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.unsafe.first.FirstInboundHandler;
import shadow.utils.objects.AlixConsoleFilterHolder;
import shadow.virtualization.BukkitLimboIntegration;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.handlers.DummyHandler;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;

public final class AlixInterceptor {

    public static final boolean PROXY_PROTOCOL = ServerEnvironment.isPaper() && GlobalConfiguration.get().proxies.proxyProtocol;
    private static final String name = "alix-interceptor";//, name2 = "AlixInjector";
    private static final LimboServer limbo;
    //private static final boolean enableLimbo = !Bukkit.getServer().getOnlineMode() && Main.config.getBoolean("virtual-limbo-server");
    private static final Interceptor interceptor;
    public static final FireWallType fireWallType;

    static {
        FireWallType type = FireWallType.NETTY;

        if (!AlixUtils.antibotService) type = FireWallType.NOT_USED;
        else if (AlixAtaraxia.ENABLED) {
            type = FireWallType.ATARAXIA;
            AlixCommonMain.logInfo("Using the optimized Alix Ataraxia for FireWall Protection.");
        } else if (!Main.config.getBoolean("unsafe-firewall")) {
            type = FireWallType.NETTY;
            AlixCommonMain.logInfo("Using Netty for FireWall Protection (per config).");
        } else if (!PROXY_PROTOCOL) {
            try {
                if (AlixHandler.isEpollTransport) {
                    AlixConsoleFilterHolder.INSTANCE.startFilteringStd();
                    try {
                        type = FireWallType.FAST_UNSAFE_EPOLL;
                        AlixFastUnsafeEpoll.init(AlixEpollConnection.class);
                        AlixCommonMain.logInfo("Using Fast Unsafe Epoll for FireWall Protection. Fast IPv4 look-ups are Enabled.");
                    } finally {
                        AlixConsoleFilterHolder.INSTANCE.stopFilteringStd();
                    }
                }
                //no longer supported
                /*else if (PlatformDependent.javaVersion() <= 8) {//before modularization
                    type = FireWallType.INTERNAL_NIO_INTERCEPTOR;
                    AlixInternalNIOInterceptor.init();
                    AlixCommonMain.logInfo("Using Internal NIO Interceptor for FireWall Protection.");
                } */
                else {
                    type = FireWallType.NETTY;
                    AlixCommonMain.logInfo("Using Netty for FireWall Protection.");
                }
            } catch (Throwable e) {
                //e.printStackTrace();
                type = FireWallType.NETTY;
                AlixCommonMain.logInfo("Using Netty for FireWall Protection - the faster implementation could not have been used!");
                if (AlixUtils.isDebugEnabled) {
                    AlixCommonMain.debug("Error that occurred when trying to use the faster implementation (send this to the developer):");
                    AlixCommonMain.debug("Epoll: " + AlixHandler.isEpollTransport);
                    e.printStackTrace();
                } else
                    AlixCommonMain.logInfo("If you wish to use the faster FireWall implementation enable 'debug' in config.yml and contact the developer!");
            }
        }

        fireWallType = type;
        FireWallType.USED.set(fireWallType);

        interceptor = new Interceptor();

        AlixHandler.SERVER_CHANNELS.forEach(channel -> {
            ChannelPipeline serverPipeline = channel.pipeline();
            injectIntoServerPipeline(serverPipeline);
        });

        /*if (!enableLimbo) {
            AlixCommonMain.logWarning("virtual-limbo-server=false is now unsupported! Enabling the limbo anyway");
        }*/

        limbo = NanoLimbo.load(new BukkitLimboIntegration());
    }

    private static void injectIntoServerPipeline(ChannelPipeline serverPipeline) {//, ChannelHandler firewallHandler) {//the server pipeline
        if (serverPipeline.context(name) != null) serverPipeline.remove(name);
        serverPipeline.addFirst(name, interceptor);//set up the new interceptor, possibly a more recent one if it was a reload and it's bytecode changed (a new version of this plugin was uploaded)
    }

    @Sharable
    private static final class Interceptor extends FirstInboundHandler {

        private static final boolean isNettyFireWall = fireWallType == FireWallType.NETTY;

        private Interceptor() {
            super(name);
            AlixChannelHandler.init();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel channel = (Channel) msg;

            if (!PROXY_PROTOCOL) {
                InetAddress address = AlixCommonUtils.getAddress(channel);
                if (FireWallManager.isBlocked0(address)) {
                    channel.unsafe().closeForcibly();
                    return;
                }
                //RATE_LIMITED/FIREWALLED both mean this specific connection shouldn't proceed either - see
                //LimboIntegration#onProxyAddress's matching comment.
                //
                //FUNCTIONALITY (audit, 2026-09-24): weighted, not the plain 1-arg overload - this call
                //site is reached exactly when PROXY_PROTOCOL is false, which is also exactly when
                //TelemetryProfilerImpl actually captures SYN signatures (see LimboIntegration
                //#connectionWeight()'s own docs) - the ONLY other caller, onProxyAddress(), is reached only
                //when PROXY_PROTOCOL is true, where a signature can never exist, so leaving this call site
                //unweighted meant the whole feature was silently dead in every real deployment.
                if (AntiBotStatistics.INSTANCE.incrementConnections(address, LimboIntegration.connectionWeight(channel)) != ConnectionVerdict.ALLOWED) {
                    channel.unsafe().closeForcibly();
                    return;
                }
            }

            //always true
            if (limbo != null) {
                ChannelConfig config = channel.config();
                ChannelPipeline pipeline = channel.pipeline();
                config.setAutoRead(false);

                //try/finally: exceptionCaught() below only logs and never rethrows, so without this, an
                //exception from any of the three calls below left autoRead disabled forever - Netty would
                //never read another byte from this one channel, hanging it permanently with no cleanup.
                try {
                    limbo.getClientChannelInitializer().initChannel(channel, PROXY_PROTOCOL, false);

                    super.channelRead(ctx, msg);
                    //Log.error("pipeline=" + channel.pipeline().names());

                    if (NanoLimbo.removeTimeout && pipeline.context("timeout") != null)
                        pipeline.replace("timeout", "--timeout", DummyHandler.HANDLER);
                } finally {
                    config.setAutoRead(true);
                }
                return;
            }

            AlixChannelHandler.inject(channel);
            super.channelRead(ctx, msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
        }

        private static void onDisable() {
            interceptor.allowRemoval();
            if (limbo != null) limbo.onDisable();
        }
    }

    public static void onDisable() {
        Interceptor.onDisable();
        //see AlixHandler#purgeNullChannelConnections()'s own docs - must run before vanilla's own
        //MinecraftServer#stopServer() gets to ServerConnectionListener#handleAllDisconnections(), which
        //this plugin disabling here happens well ahead of.
        AlixHandler.purgeNullChannelConnections();
        switch (fireWallType) {
            case NETTY:
                AlixHandler.SERVER_CHANNELS.forEach(channel -> {
                    ChannelPipeline pipeline = channel.pipeline();
                    if (pipeline.context(name) != null) pipeline.remove(name);
                });
                break;
            /*case INTERNAL_NIO_INTERCEPTOR:
                AlixInternalNIOInterceptor.unregister();
                break;*/
            case FAST_UNSAFE_EPOLL:
                break;
        }
    }

    public static void init() {
    }
}