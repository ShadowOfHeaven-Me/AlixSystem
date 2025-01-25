package alix.velocity.server;

import alix.velocity.server.impl.VelocityLimboIntegration;
import io.netty.channel.Channel;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.LimboServer;

public final class AlixVelocityLimbo {

    private static final LimboServer limbo = NanoLimbo.load(new VelocityLimboIntegration());

    public static void initChannel(Channel channel) {
        limbo.getClientChannelInitializer().initChannel(channel);
    }

    public static void init() {
    }
}