package alix.velocity.systems.channel;

import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.FireWallType;
import alix.common.connection.ConnectionThreadManager;
import alix.velocity.server.AlixVelocityLimbo;
import alix.velocity.utils.AlixUtils;
import com.velocitypowered.proxy.VelocityServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Method;
import java.net.InetAddress;

public final class ServerChannelInitializer extends com.velocitypowered.proxy.network.ServerChannelInitializer {

    //public static final Class<?> EXTENDING_CLASS = ServerChannelInitializer.class;
    //private final BackendChannelInitializer original;
    private static final boolean isNettyFireWall;
    private static final Method initChannelMethod;

    static {
        FireWallType used = FireWallType.NETTY;
        FireWallType.USED.set(used);

        isNettyFireWall = used == FireWallType.NETTY;
        try {
            initChannelMethod = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            initChannelMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final ChannelInitializer<Channel> original;

    public ServerChannelInitializer(VelocityServer server, ChannelInitializer<Channel> original) {
        super(server);
        this.original = original;
    }

    @Override
    protected void initChannel(Channel channel) {
        InetAddress address = AlixUtils.getAddress(channel);
        if (isNettyFireWall && address != null) {
            if (FireWallManager.isBlocked(address)) {
                channel.unsafe().closeForcibly();
                return;
            }
        }
        AntiBotStatistics.INSTANCE.incrementJoins();
        if (address != null) ConnectionThreadManager.onConnection(address);

        //var config = channel.config();
        //config.setAutoRead(false);
        try {
            initChannelMethod.invoke(this.original, channel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        AlixVelocityLimbo.initChannel(channel);
        //config.setAutoRead(true);

        //Main.logInfo("Pipeline: " + channel.pipeline().names());
    }
}