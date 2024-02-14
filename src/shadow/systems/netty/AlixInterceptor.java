package shadow.systems.netty;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.firewall.FireWallManager;
import io.netty.channel.*;
import shadow.Main;
import shadow.utils.main.AlixUtils;

import java.net.InetSocketAddress;

public final class AlixInterceptor {

    private static final String name = "AlixInterceptor";//, name2 = "AlixInjector";

    public static void injectIntoServerPipeline(ChannelPipeline serverPipeline, ChannelHandler firewallHandler) {//the server pipeline
        if (serverPipeline.get(name) != null)//the check is necessary because of a NoSuchElementException otherwise
            serverPipeline.remove(name);//remove the interceptor on reload to prevent it from stacking
        serverPipeline.addFirst(name, new Interceptor(firewallHandler));//set up the new interceptor, possibly a more recent one if it was a reload and it's bytecode changed (a new version of this plugin was uploaded)

        /*if (serverPipeline.get(name2) != null)
            serverPipeline.remove(name2);
        serverPipeline.addAfter(name, name2, new AlixChannelInjector());*/
    }

    private static final class Interceptor extends ChannelInboundHandlerAdapter {

        private static final String fireWallName = "AlixDelayedChannelFireWall";
        private final ChannelHandler delayedFirewall;

        private Interceptor(ChannelHandler delayedFirewall) {
            this.delayedFirewall = delayedFirewall;
            if (FireWallManager.isOsFireWallInUse)
                AlixCommonMain.logInfo("Using the optimized OS IpSet for FireWall Protection.");
            else
                AlixCommonMain.logInfo("Using Netty for FireWall Protection initialization. Fast-Mode: " + ((delayedFirewall == null) ? "ON" : "OFF"));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //if (msg instanceof Channel) {
            Channel channel = (Channel) msg;
            AntiBotStatistics.INSTANCE.incrementJoins();
            //Main.logError("Channel creation: " + channel);
            if (!AlixUtils.antibotService || !FireWallManager.isBlocked((InetSocketAddress) channel.unsafe().remoteAddress())) {
                //Main.logWarning("STARTED INJECTING " + ctx.name());
                AlixChannelInjector.inject(channel);
                super.channelRead(ctx, msg);
                //Main.logWarning("INJECTION LISTENER ACTIVATED " + ctx.name());
                return;
            }
            if (delayedFirewall == null) {
                channel.unsafe().closeForcibly();//the fastest and probably the only way to close the channel in this phase
                return;
            }
            channel.pipeline().addLast(fireWallName, this.delayedFirewall);//the delayed firewall handling
            //}
            super.channelRead(ctx, msg);
            //Main.logInfo(
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            //Main.logError("SEXXXXXXXXXXXXX");
            cause.printStackTrace();
        }
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