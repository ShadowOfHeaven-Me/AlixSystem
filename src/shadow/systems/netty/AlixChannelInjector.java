package shadow.systems.netty;

import alix.common.antibot.firewall.FireWallManager;
import com.google.common.collect.MapMaker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import shadow.Main;
import shadow.systems.dependencies.Dependencies;
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
        channel.pipeline().addFirst(CHANNEL_ACTIVE_LISTENER_NAME, channelActiveListener);//wait for channel active, as we cannot inject out packet listener before that, since the server hasn't injected the channel yet
    }

    @Sharable
    private static final class ChannelActiveListener extends ChannelDuplexHandler {

        private final String injectBefore;

        private ChannelActiveListener() {
            if (Dependencies.isProtocolLibPresent) Main.logInfo("Fixing a specific ProtocolLib injection problem.");
            this.injectBefore = Dependencies.isProtocolLibPresent ? "protocol_lib_inbound_interceptor" : "packet_handler";
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            //Main.logWarning("LISTENER ACTIVUH " + ctx.name() + " " + ctx.pipeline().names());
            ctx.channel().pipeline().addBefore(injectBefore, PACKET_INJECTOR_NAME, packetInjector);//now we can add the temporary packet listener
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.channel().close();
            //Main.logWarning("LISTENER ACTIVATE ERROR " + cause.toString());
            //cause.printStackTrace();
            //super.exceptionCaught(ctx, cause);
        }

        /*@Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            Main.logError("CHANNEL REGISTERED: " + ctx.name());
            super.channelRegistered(ctx);
        }*/

        /*@Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            Main.logWarning("PRE-TO-ADDED " + ctx.pipeline().names());
            super.handlerAdded(ctx);
            Main.logWarning("ADDED " + ctx.pipeline().names());
        }*/
    }

    @Sharable
    private static final class PacketInjectorImpl extends ChannelDuplexHandler {

        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //Main.logInfo("PACKET: " + msg.getClass().getSimpleName());
            if (msg.getClass() == ReflectionUtils.loginInStartPacketClass)//this packet contains the player's nickname
                //Main.logInfo("PRE TO ADD NAME: " + LoginInStartPacketGetter.getPlayerName(msg));
                CHANNELS.put(LoginInStartPacketGetter.getPlayerName(msg), ctx.channel());
            //Main.logInfo("CHANNEL ADDED FOR NAME: " + LoginInStartPacketGetter.getPlayerName(msg));

            super.channelRead(ctx, msg);
        }

        //private final AlixMessage firewalled = Messages.getAsObject("anti-ddos-fail-console-message", "{0}", "InvalidDecoder");

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            //if(cause.getClass() != DecoderException.class) Main.logWarning("ERROR ON INTERCEPTING: " + cause.toString());
            //Main.logWarning("ERROR ON INTERCEPTING: " + cause.toString());
            //cause.printStackTrace();
            if (cause.getClass() == DecoderException.class)// && cause.getCause() != null && cause.getCause().getClass() == IndexOutOfBoundsException.class)
                FireWallManager.addCauseException(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
            //ctx.channel().close();
            //Main.logInfo(this.firewalled.format(ip));
            //if (cause.getClass() != ReadTimeoutException.class) ctx.channel().close();
        }
    }

    private AlixChannelInjector() {
    }
}