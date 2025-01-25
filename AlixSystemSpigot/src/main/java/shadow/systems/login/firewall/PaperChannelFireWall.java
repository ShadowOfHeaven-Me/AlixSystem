/*
package shadow.systems.login.firewall;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.Messages;
import io.netty.channel.*;
import io.papermc.paper.network.ChannelInitializeListener;
import shadow.Main;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;

import java.net.InetSocketAddress;

public final class PaperChannelFireWall implements ChannelInitializeListener {

    private static final Object kickPacket = OutDisconnectKickPacketConstructor.constructAtLoginPhase(Messages.get("anti-bot-firewalled"));
    private static final String interceptorName = "alixsystem_firewall";
    private final FirewallPacketInterceptor INTERCEPTOR = new FirewallPacketInterceptor();
    private final boolean fastRaw = Main.config.getBoolean("fast-raw-firewall");

    public PaperChannelFireWall() {
        Main.logInfo("Using Paper for FireWall Protection initialization. Fast-Mode: " + (fastRaw ? "ON" : "OFF"));
    }

    @Override
    public final void afterInitChannel(Channel channel) {
        //Main.logError("PAPER CHANNEL");
        if (!FireWallManager.isBlocked((InetSocketAddress) channel.remoteAddress())) return;
        if (fastRaw) channel.close();
        else channel.pipeline().addBefore("packet_handler", interceptorName, INTERCEPTOR);
    }

    //Main.logError("Channel: " + channel.getClass().getSimpleName() + " " + ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress() + " " + ((InetSocketAddress) channel.localAddress()).getAddress().getHostAddress());

    @ChannelHandler.Sharable
    private static final class FirewallPacketInterceptor extends ChannelDuplexHandler {

        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg.getClass() == ReflectionUtils.loginInStartPacketClass)
                ctx.channel().writeAndFlush(kickPacket).addListener(ChannelFutureListener.CLOSE);
            else super.channelRead(ctx, msg);
        }

        //Main.logInfo(msg.getClass().getSimpleName());

*/
/*        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            Main.logInfo(msg.getClass().getSimpleName());
            //PacketStatusOutServerInfo
            super.write(ctx, msg, promise);
        }*//*

    }
}*/
