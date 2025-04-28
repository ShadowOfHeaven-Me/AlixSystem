package shadow.systems.netty;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.firewall.AlixOSFireWall;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.FireWallType;
import alix.common.utils.other.throwable.AlixException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.unix.AlixFastUnsafeEpoll;
import org.bukkit.Bukkit;
import shadow.Main;
import shadow.systems.netty.unsafe.nio.AlixInternalNIOInterceptor;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.unsafe.first.FirstInboundHandler;
import shadow.utils.objects.AlixConsoleFilterHolder;
import shadow.virtualization.BukkitLimboIntegration;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.handlers.DummyHandler;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public final class AlixInterceptor {

    private static final String name = "alix-interceptor";//, name2 = "AlixInjector";
    private static final LimboServer limbo;
    private static final boolean enableLimbo = !Bukkit.getServer().getOnlineMode() && Main.config.getBoolean("virtual-limbo-server");
    private static final Interceptor interceptor;
    private static final ChannelHandlerContext silentServerCtx;
    public static final FireWallType fireWallType;

    static {
        FireWallType type;

        if (!AlixUtils.antibotService) type = FireWallType.NOT_USED;
        else if (AlixOSFireWall.isOsFireWallInUse) {
            type = FireWallType.OS_IPSET;
            AlixCommonMain.logInfo("Using the optimized OS IpSet for FireWall Protection.");
        } else {
            try {
                if (AlixHandler.isEpollTransport) {
                    AlixConsoleFilterHolder.INSTANCE.startFilteringStdErr();
                    try {
                        type = FireWallType.FAST_UNSAFE_EPOLL;
                        AlixFastUnsafeEpoll.init();
                        AlixCommonMain.logInfo("Using Fast Unsafe Epoll for FireWall Protection. Fast IPv4 look-ups are Enabled.");
                    } finally {
                        AlixConsoleFilterHolder.INSTANCE.stopFilteringStdErr();
                    }
                } else {
                    type = FireWallType.INTERNAL_NIO_INTERCEPTOR;
                    AlixInternalNIOInterceptor.init();
                    AlixCommonMain.logInfo("Using Internal NIO Interceptor for FireWall Protection.");
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

        ChannelPipeline serverPipeline = AlixHandler.SERVER_CHANNEL.pipeline();
        interceptor = new Interceptor();
        injectIntoServerPipeline(serverPipeline);

        silentServerCtx = serverPipeline.context(name);
        //Main.debug("SILENT CTX: " + silentServerCtx.name() + " HASH: " + System.identityHashCode(silentServerCtx));
        //Main.debug("HANDLERS: " + serverPipeline.toMap());

        //https://github.com/Nan1t/NanoLimbo/blob/main/src/main/java/ua/nanit/limbo/server/LimboServer.java

        limbo = enableLimbo && AlixUtils.requireCaptchaVerification ? NanoLimbo.load(new BukkitLimboIntegration()) : null;
    }

    private static void injectIntoServerPipeline(ChannelPipeline serverPipeline) {//, ChannelHandler firewallHandler) {//the server pipeline
        if (serverPipeline.context(name) != null) serverPipeline.remove(name);
        serverPipeline.addFirst(name, interceptor);//set up the new interceptor, possibly a more recent one if it was a reload and it's bytecode changed (a new version of this plugin was uploaded)
        //AlixInjector.injectIntoServerPipeline(serverPipeline);
        //Main.logError("SERVER NAMES " + serverPipeline.names());
        //Main.logError("SERVER NAME " + PacketEvents.SERVER_CHANNEL_HANDLER_NAME);

        /*if (serverPipeline.get(name2) != null)
            serverPipeline.remove(name2);
        serverPipeline.addAfter(name, name2, new AlixChannelInjector());*/
    }

    @Sharable
    private static final class Interceptor extends FirstInboundHandler {

        //private static final String delayedFireWallName = "AlixDelayedChannelFireWall";
        //private final ChannelHandler delayedFirewall;
        //private final boolean isImmediate;
        private static final boolean isNettyFireWall = fireWallType == FireWallType.NETTY;

        private Interceptor() {
            super(name);
            //this.delayedFirewall = delayedFirewall;
            //this.isImmediate = delayedFirewall == null;
            //Just like CompletableFuture
            //Class<?> ensureLoaded = AlixChannelInjector.class;

            //if (AlixHandler.isEpollTransport) AlixEpollEventLoop.override();

            AlixChannelHandler.init();
        }
        //        private static final LimboServer limbo = AlixUtils.requireCaptchaVerification
//                ? NanoLimbo.load(AlixHandler.SERVER_CHANNEL_FUTURE.channel(), new LimboServerIntegration())
//                : null;
//
//        static {
//            /*AlixHandler.SERVER_CHANNEL_FUTURE.channel().pipeline().addFirst(new ChannelInitializer<>() {
//
//                @Override
//                protected void initChannel(Channel channel) throws Exception {
//                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
//                    channel.config().setOption(ChannelOption.AUTO_READ, true);
//                }
//            });*/
//            //Main.debug("SERVER PIPELINE: " + AlixHandler.SERVER_CHANNEL_FUTURE.channel().pipeline().names());
//        }

        private void invokeSilentChannelRead(ChannelHandlerContext ctx, Channel ch) {
            try {
                super.channelRead(ctx, ch);
            } catch (Exception e) {
                throw new AlixException(e);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel channel = (Channel) msg;
            //Main.logError("CHANNEL: " + channel + " HASH: " + channel.hashCode() + " SERVER CHANNEL: " + (channel instanceof ServerChannel));
            //Main.logError("CHANNEL IDENTITY HASH: " + System.identityHashCode(channel) + " CLAZZ: " + msg.getClass().getSimpleName());
            //AlixUtils.debug(Thread.currentThread().getStackTrace());
            //Main.logError("CTX NAME: " + ctx.name() + " HASH: " + System.identityHashCode(ctx));
            AntiBotStatistics.INSTANCE.incrementJoins();
            InetAddress address = ((InetSocketAddress) channel.unsafe().remoteAddress()).getAddress();
            if (isNettyFireWall) {
                //Channel serverChannel = AlixHandler.SERVER_CHANNEL_FUTURE.channel();
                //Main.logInfo(channel + " " + serverChannel + " " + serverChannel.eventLoop());
                if (FireWallManager.isBlocked(address)) {
                    channel.unsafe().closeForcibly();
                    return;
                }
            }

            if (enableLimbo && AlixUtils.requireCaptchaVerification/*&& !LimboServerIntegration.hasCompletedCaptcha(address)*/) {
                //Main.debug("Initializing the mfo");

                ChannelConfig config = channel.config();
                ChannelPipeline pipeline = channel.pipeline();
                //Main.debug("AUTO READ " + channel.config().isAutoRead());
                config.setAutoRead(false);

                //if (config.isAutoRead()) config.setAutoRead(false);

                limbo.getClientChannelInitializer().initChannel(channel);
                super.channelRead(ctx, msg);
                //Main.logError("PIPELINE: " + pipeline.names());

                if (NanoLimbo.removeTimeout && pipeline.context("timeout") != null)
                    pipeline.replace("timeout", "--timeout", DummyHandler.HANDLER);

                config.setAutoRead(true);

                //Main.debug("MFO HANDLERS: " + channel.pipeline().names() + " THREAD: " + Thread.currentThread());

                //config.setAutoRead(false);

                //prevent a race condition, where the server could read the packets without our handler being injected yet
                //if (config.isAutoRead()) config.setAutoRead(false);

                //Remove TimeOut handler

                //if (pipeline.context("timeout") != null) pipeline.replace("timeout", "--timeout", NoTimeoutHandler.INSTANCE); //new NoTimeoutHandler(timeoutCtx.handler()));

                /*for (String name : list) {
                    if (name.equals("DefaultChannelPipeline$TailContext#0")) return;
                    Main.debug("REMOVING " + name);
                    try {
                        channel.pipeline().remove(name);
                    } catch (Throwable ignored) {
                        Main.debug("NOT FOUND " + name);
                    }
                }*/
                //Main.debug("MMMMMMM: " + channel.pipeline().names());

                //Main.debug("REMOVED: " + channel.pipeline().names());


                //Main.debug("INITIALIZED: " + channel.pipeline().names());
                return;
            }

            //Main.logError("CHANNELLLLLLLLLLLLLLLLLLLLLL: " + channel);

            //VirtualServer.init(channel);
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

    public static void invokeSilentChannelRead(Channel channel) {
        interceptor.invokeSilentChannelRead(silentServerCtx, channel);
    }

    public static void onDisable() {
        Interceptor.onDisable();
        switch (fireWallType) {
            case NETTY:
                ChannelPipeline pipeline = AlixHandler.SERVER_CHANNEL.pipeline();
                if (pipeline.context(name) != null) pipeline.remove(name);
                break;
            case INTERNAL_NIO_INTERCEPTOR:
                AlixInternalNIOInterceptor.unregister();
                break;
            case FAST_UNSAFE_EPOLL:
                break;
        }
    }

    public static void init() {
    }

/*    public static void initialize() {
        int port = AbstractServer.INSTANCE.getPort();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new Interceptor());

            Channel channel = serverBootstrap.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new AlixException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }*/

/*    public static Interceptor createInterceptor() {
        return new Interceptor();
    }*/
}