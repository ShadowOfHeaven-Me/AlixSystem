package alix.velocity.systems.filters.firewall;

import alix.common.antibot.firewall.FireWallManager;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ServerChannelInitializer;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public final class AlixChannelFireWall extends ServerChannelInitializer {

    public static final Class<?> EXTENDING_CLASS = ServerChannelInitializer.class;
    //private final BackendChannelInitializer original;

    public AlixChannelFireWall(VelocityServer server) {
        super(server);
        //this.original = original;
    }

    @Override
    protected final void initChannel(Channel ch) {
        if (FireWallManager.isBlocked((InetSocketAddress) ch.unsafe().remoteAddress())) ch.close();
        else super.initChannel(ch);
    }

/*    private static final class ChannelAccessor extends BackendChannelInitializer {

        private final BackendChannelInitializer original;

        public ChannelAccessor(BackendChannelInitializer original) {
            super(null);
            this.original = original;
        }

        @Override
        protected void initChannel(Channel ch) throws Exception {

        }
    }*/
}