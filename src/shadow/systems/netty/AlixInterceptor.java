package shadow.systems.netty;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.firewall.AlixOSFireWall;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.FireWallType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.unix.AlixFastUnsafeEpoll;
import org.jetbrains.annotations.TestOnly;
import shadow.systems.netty.unsafe.nio.AlixInternalNIOInterceptor;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.unsafe.first.FirstInboundHandler;
import shadow.utils.objects.AlixConsoleFilterHolder;

import java.net.InetSocketAddress;

public final class AlixInterceptor {

    private static final String name = "alix-interceptor";//, name2 = "AlixInjector";
    public static final FireWallType fireWallType;

    static {
        FireWallType type;

        if (AlixOSFireWall.isOsFireWallInUse) {
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
                } else AlixCommonMain.logInfo("If you wish to use the faster FireWall implementation enable 'debug' in config.yml and contact the developer!");
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

        @TestOnly
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel channel = (Channel) msg;

            //VirtualServer.init(channel);

            if (isNettyFireWall) {
                AntiBotStatistics.INSTANCE.incrementJoins();
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
                AlixHandler.SERVER_CHANNEL_FUTURE.channel().pipeline().remove(name);
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