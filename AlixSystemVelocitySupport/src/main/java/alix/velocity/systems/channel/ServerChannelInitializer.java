package alix.velocity.systems.channel;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.any.ConnectRequestAlgoImpl;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.epoll.AlixEpollConnection;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.FireWallType;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.throwable.AlixException;
import alix.velocity.Main;
import alix.velocity.server.AlixVelocityLimbo;
import com.github.retrooper.packetevents.PacketEvents;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.TransportType;
import io.github.retrooper.packetevents.handlers.PacketEventsEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.unix.AlixFastUnsafeEpoll;
import ua.nanit.limbo.NanoLimbo;

import java.lang.reflect.Method;
import java.net.InetAddress;

public final class ServerChannelInitializer extends com.velocitypowered.proxy.network.ServerChannelInitializer {

    //public static final Class<?> EXTENDING_CLASS = ServerChannelInitializer.class;
    //private final BackendChannelInitializer original;
    public static final boolean PROXY_PROTOCOL = Main.SERVER.getConfiguration().isProxyProtocol();
    private static final boolean isNettyFireWall;
    private static final Method initChannelMethod;

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

        try {
            initChannelMethod = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            initChannelMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final ChannelInitializer<Channel> original;
    final VelocityServer server;

    static ServerChannelInitializer INITIALIZER;

    public ServerChannelInitializer(VelocityServer server, ChannelInitializer<Channel> original) {
        super(server);
        this.server = server;
        this.original = original;
        INITIALIZER = this;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = (Channel) msg;
        if (!PROXY_PROTOCOL) {
            InetAddress address = AlixCommonUtils.getAddress(channel);
            if (isNettyFireWall && address != null) {
                if (FireWallManager.isBlocked0(address)) {
                    channel.unsafe().closeForcibly();
                    return;
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void initChannel(Channel channel) {
        AntiBotStatistics.INSTANCE.incrementJoins();

        if (!PROXY_PROTOCOL) {
            InetAddress address = AlixCommonUtils.getAddress(channel);
            if (address != null) {
                if (ConnectRequestAlgoImpl.onConnection(channel, address))
                    return;
            }
        }

        /*var config = channel.config();
        config.setAutoRead(false);
        try {
            initChannelMethod.invoke(this.original, channel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
        AlixVelocityLimbo.initChannel(channel, PROXY_PROTOCOL);
        //config.setAutoRead(true);

        //Main.logInfo("Pipeline: " + channel.pipeline().names());
    }

    public static void invokeOriginalChannelInit(Channel channel) {
        try {
            initChannelMethod.invoke(INITIALIZER.original, channel);
        } catch (Exception e) {
            throw new AlixException(e);
        }

        if (!NanoLimbo.debugMode) return;

        var pipeline = channel.pipeline();
        var original = (PacketEventsEncoder) pipeline.context(PacketEvents.ENCODER_NAME).handler();
        pipeline.replace(PacketEvents.ENCODER_NAME, PacketEvents.ENCODER_NAME, new EncoderMonitor(original));
    }
}