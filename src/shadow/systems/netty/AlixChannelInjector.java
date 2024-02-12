package shadow.systems.netty;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import com.google.common.collect.MapMaker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.packet.getters.LoginInStartPacketGetter;

import java.net.InetSocketAddress;
import java.util.Map;

import static io.netty.channel.ChannelHandler.Sharable;

public final class AlixChannelInjector {

    public static final Map<String, Channel> CHANNELS = new MapMaker().weakValues().makeMap();//ensure we do not hold the Channel reference captive by using weak values
    public static final String
            CHANNEL_ACTIVE_LISTENER_NAME = "AlixChannelActiveListener",
            PACKET_INJECTOR_NAME = "AlixPacketChannelInjector";
    private static final ChannelActiveListener channelActiveListener = new ChannelActiveListener();
    private static final PacketInjectorImpl packetInjector = new PacketInjectorImpl();

    public static void inject(Channel channel) {
        channel.pipeline().addLast(CHANNEL_ACTIVE_LISTENER_NAME, channelActiveListener);//wait for channel active, as we cannot inject out packet listener before that, since the server hasn't injected the channel yet
    }

    @Sharable
    private static final class ChannelActiveListener extends ChannelDuplexHandler {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Channel channel = ctx.channel();
            channel.pipeline().addBefore("packet_handler", PACKET_INJECTOR_NAME, packetInjector);//now we can add the temporary packet listener
            //Main.logWarning("LISTENER ACTIVUH " + ctx.name());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.channel().close();
            //Main.logWarning("LISTENER ACTIVUH ERROR " + ctx.name());
            //super.exceptionCaught(ctx, cause);
        }
    }

    @Sharable
    private static final class PacketInjectorImpl extends ChannelDuplexHandler {

        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg.getClass() == ReflectionUtils.loginInStartPacketClass)//this packet contains the player's nickname
                CHANNELS.put(LoginInStartPacketGetter.getPlayerName(msg), ctx.channel());
            super.channelRead(ctx, msg);
        }

        //private final AlixMessage firewalled = Messages.getAsObject("anti-ddos-fail-console-message", "{0}", "InvalidDecoder");

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            //if(cause.getClass() != DecoderException.class) Main.logWarning("ERROR ON INTERCEPTING: " + cause.toString());
            if (cause.getClass() == DecoderException.class)
                FireWallManager.addCauseException(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress(), cause);
            //Main.logInfo(this.firewalled.format(ip));
            ctx.channel().close();
        }
    }

    private AlixChannelInjector() {
    }
}