package shadow.systems.netty;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.epoll.AlixEpollConnection;
import alix.common.antibot.firewall.AlixOSFireWall;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.FireWallType;
import alix.common.environment.ServerEnvironment;
import alix.common.utils.AlixCommonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.unix.AlixFastUnsafeEpoll;
import io.papermc.paper.configuration.GlobalConfiguration;
import org.bukkit.Bukkit;
import shadow.Main;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.unsafe.first.FirstInboundHandler;
import shadow.utils.objects.AlixConsoleFilterHolder;
import shadow.virtualization.BukkitLimboIntegration;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.handlers.DummyHandler;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;

public final class AlixInterceptor {

    public static final boolean PROXY_PROTOCOL = ServerEnvironment.isPaper() && GlobalConfiguration.get().proxies.proxyProtocol;
    private static final String name = "alix-interceptor";//, name2 = "AlixInjector";
    private static final LimboServer limbo;
    private static final boolean enableLimbo = !Bukkit.getServer().getOnlineMode() && Main.config.getBoolean("virtual-limbo-server");
    private static final Interceptor interceptor;
    public static final FireWallType fireWallType;

    static {
        FireWallType type = FireWallType.NETTY;

        if (!AlixUtils.antibotService) type = FireWallType.NOT_USED;
        else if (AlixOSFireWall.isOsFireWallInUse) {
            type = FireWallType.OS_IPSET;
            AlixCommonMain.logInfo("Using the optimized OS IpSet for FireWall Protection.");
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

        if (!enableLimbo) {
            AlixCommonMain.logWarning("virtual-limbo-server=false is now unsupported! Enabling the limbo anyway");
        }

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

            //Main.debug("CHANNEL CONNECT=" + channel);
            InetAddress address = AlixCommonUtils.getAddress(channel);
            AntiBotStatistics.INSTANCE.incrementJoins();

            if (!PROXY_PROTOCOL && address != null) {
                if (isNettyFireWall) {
                    if (FireWallManager.isBlocked0(address)) {
                        channel.unsafe().closeForcibly();
                        return;
                    }
                }
                //ConnectRequestAlgoImpl.onUnregisteredConnection(channel);
            }

            //always true
            if (limbo != null) {
                ChannelConfig config = channel.config();
                ChannelPipeline pipeline = channel.pipeline();
                config.setAutoRead(false);

                limbo.getClientChannelInitializer().initChannel(channel, PROXY_PROTOCOL, false);

                super.channelRead(ctx, msg);
                //Log.error("pipeline=" + channel.pipeline().names());

                if (NanoLimbo.removeTimeout && pipeline.context("timeout") != null)
                    pipeline.replace("timeout", "--timeout", DummyHandler.HANDLER);

                config.setAutoRead(true);
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