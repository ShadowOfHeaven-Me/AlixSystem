package alix.velocity.systems.channel;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.epoll.AlixEpollConnection;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.FireWallType;
import alix.common.utils.AlixCommonUtils;
import alix.velocity.Main;
import alix.velocity.server.AlixVelocityLimbo;
import com.velocitypowered.proxy.network.TransportType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.unix.AlixFastUnsafeEpoll;

import java.net.InetAddress;

public final class ServerChannelInitializer extends ChannelInboundHandlerAdapter {//com.velocitypowered.proxy.network.ServerChannelInitializer {

    //public static final Class<?> EXTENDING_CLASS = ServerChannelInitializer.class;
    //private final BackendChannelInitializer original;
    public static final boolean PROXY_PROTOCOL = Main.SERVER.getConfiguration().isProxyProtocol();
    private static final boolean isNettyFireWall;
    //private static final Method initChannelMethod;

    static {
        FireWallType used = FireWallType.NETTY;

        //per https://github.com/PaperMC/Velocity/blob/dev/3.0.0/proxy/src/main/java/com/velocitypowered/proxy/network/ConnectionManager.java#L78
        //cannot use epoll firewall in case of proxy set-up - we need to wait anyway
        if (!PROXY_PROTOCOL && TransportType.bestType() == TransportType.EPOLL) {//PacketEvents.getAPI().getServerManager().getOS() == SystemOS.LINUX
            try {
                AlixFastUnsafeEpoll.init(AlixEpollConnection.class);
                used = FireWallType.FAST_UNSAFE_EPOLL;
                AlixCommonMain.logInfo("Using Fast Unsafe Epoll for FireWall Protection. Fast IPv4 look-ups are Enabled.");
            } catch (Throwable ignored) {
            }
        }

        isNettyFireWall = used == FireWallType.NETTY;
        if (isNettyFireWall)
            AlixCommonMain.logInfo("Using Netty for FireWall Protection.");

        FireWallType.USED.set(used);

        /*try {
            initChannelMethod = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            initChannelMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }*/
    }

    //private final ChannelInitializer<Channel> original;

    //static ServerChannelInitializer INITIALIZER;

    public ServerChannelInitializer() {
        //super(server);
        //this.original = original;
        //INITIALIZER = this;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = (Channel) msg;
        AntiBotStatistics.INSTANCE.incrementJoins();
        if (!PROXY_PROTOCOL) {
            InetAddress address = AlixCommonUtils.getAddress(channel);
            if (isNettyFireWall) {
                if (FireWallManager.isBlocked0(address)) {
                    channel.unsafe().closeForcibly();
                    return;
                }
            }
            //ConnectRequestAlgoImpl.onUnregisteredConnection(channel);
        }

        AlixVelocityLimbo.initChannel(channel, PROXY_PROTOCOL);
        super.channelRead(ctx, msg);
        //channel.parent().eventLoop().parent().next();
    }

    /*public static void invokeOriginalChannelInit(Channel channel) {
        if (true)
            return;

        try {
            initChannelMethod.invoke(INITIALIZER.original, channel);
        } catch (Exception e) {
            throw new AlixException(e);
        }

        if (!NanoLimbo.debugMode) return;

        var pipeline = channel.pipeline();
        var original = (PacketEventsEncoder) pipeline.context(PacketEvents.ENCODER_NAME).handler();
        pipeline.replace(PacketEvents.ENCODER_NAME, PacketEvents.ENCODER_NAME, new EncoderMonitor(original));
    }*/
}