package shadow.systems.netty;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.firewall.AlixOSFireWall;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.FireWallType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.unix.AlixFastUnsafeEpoll;
import nanolimbo.alix.NanoLimbo;
import nanolimbo.alix.connection.pipeline.NoTimeoutHandler;
import nanolimbo.alix.server.LimboServer;
import shadow.Main;
import shadow.systems.netty.unsafe.nio.AlixInternalNIOInterceptor;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.unsafe.first.FirstInboundHandler;
import shadow.utils.objects.AlixConsoleFilterHolder;
import shadow.virtualization.LimboServerIntegration;

import java.net.InetSocketAddress;
import java.util.List;

public final class AlixInterceptor {

    private static final String name = "alix-interceptor";//, name2 = "AlixInjector";
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
    }

    public static void injectIntoServerPipeline(ChannelPipeline serverPipeline) {//, ChannelHandler firewallHandler) {//the server pipeline
        if (serverPipeline.context(name) != null) serverPipeline.remove(name);
        serverPipeline.addFirst(name, new Interceptor());//set up the new interceptor, possibly a more recent one if it was a reload and it's bytecode changed (a new version of this plugin was uploaded)
        //AlixInjector.injectIntoServerPipeline(serverPipeline);
        //Main.logError("SERVER NAMES " + serverPipeline.names());
        //Main.logError("SERVER NAME " + PacketEvents.SERVER_CHANNEL_HANDLER_NAME);

        /*if (serverPipeline.get(name2) != null)
            serverPipeline.remove(name2);
        serverPipeline.addAfter(name, name2, new AlixChannelInjector());*/
    }


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

        //https://github.com/Nan1t/NanoLimbo/blob/main/src/main/java/ua/nanit/limbo/server/LimboServer.java
        private static final LimboServer limbo = AlixUtils.requireCaptchaVerification
               ? NanoLimbo.load(AlixHandler.SERVER_CHANNEL_FUTURE.channel(), new LimboServerIntegration())
                : null;
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

        private static final boolean testing = true;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel channel = (Channel) msg;

            if (testing && AlixUtils.requireCaptchaVerification) {
                Main.debug("Initializing the mfo");

                ChannelConfig config = channel.config();
                ChannelPipeline pipeline = channel.pipeline();

                if (config.isAutoRead())
                    config.setAutoRead(false);

                super.channelRead(ctx, msg);

                //prevent a race condition, where the server could read the packets without our handler being injected yet
                if (config.isAutoRead())
                    config.setAutoRead(false);

                //Remove TimeOut handler
                ChannelHandlerContext timeoutCtx = pipeline.context("timeout");
                if (timeoutCtx != null)
                    pipeline.replace("timeout", "timeout", new NoTimeoutHandler(timeoutCtx.handler()));

                List<String> list = channel.pipeline().names();

                Main.debug("MFO HANDLERS: " + list + " THREAD: " + Thread.currentThread() + " EVENT LOOP THREAD: " + channel.eventLoop());

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

                limbo.getClientChannelInitializer().initChannel(channel);
                config.setAutoRead(true);
                Main.debug("INITIALIZED: " + channel.pipeline().names());
                return;
            }

            //Main.logError("CHANNELLLLLLLLLLLLLLLLLLLLLL: " + channel);

            //VirtualServer.init(channel);
            AntiBotStatistics.INSTANCE.incrementJoins();
            if (isNettyFireWall) {
                //Channel serverChannel = AlixHandler.SERVER_CHANNEL_FUTURE.channel();
                //Main.logInfo(channel + " " + serverChannel + " " + serverChannel.eventLoop());
                if (FireWallManager.isBlocked((InetSocketAddress) channel.unsafe().remoteAddress())) {
                    channel.unsafe().closeForcibly();
                    return;
                }
            }
            AlixChannelHandler.inject(channel);
            super.channelRead(ctx, msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
        }
    }

    public static void onDisable() {
        switch (fireWallType) {
            case NETTY:
                ChannelPipeline pipeline = AlixHandler.SERVER_CHANNEL_FUTURE.channel().pipeline();
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