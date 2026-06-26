package shadow.systems.netty;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.Messages;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.annotation.OptimizationCandidate;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.DecoderException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.UserManager;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.channel.ChannelHandler.Sharable;

public final class AlixChannelHandler {

    //public static final Map<String, Channel> CHANNELS = new MapMaker().weakValues().makeMap();//ensure we do not hold the Channel reference captive by using weak values
    private static final Map<Channel, ScheduledFuture<?>> TIMEOUT_TASKS = new ConcurrentHashMap<>();//we fully control this map, so no need for weak reference usage
    public static final String
            CHANNEL_ACTIVE_LISTENER_NAME = "alix-active-listener",
            CHANNEL_MONITOR_NAME = "alix-channel-monitor";
    private static final ChannelActiveListener CHANNEL_ACTIVE_LISTENER = new ChannelActiveListener();
    private static final ChannelMonitor CHANNEL_MONITOR = new ChannelMonitor();

    @Deprecated
    public static void inject(Channel channel) {
        channel.pipeline().addLast(CHANNEL_ACTIVE_LISTENER_NAME, CHANNEL_ACTIVE_LISTENER);//wait for channel active, as we cannot inject out packet listener before that, since the server hasn't injected the channel yet
    }

/*    public static void uninject(ChannelPipeline pipeline) {
        //Main.logWarning("OPEN  " + pipeline.channel().isOpen());

        pipeline.remove(CHANNEL_ACTIVE_LISTENER_NAME);//unregisters itself for some reason
        pipeline.remove(CHANNEL_MONITOR_NAME);//same as this mfo

        //pipeline.remove(PACKET_ANALYSER_NAME);
    }*/

    //remake it with channel close future
    @OptimizationCandidate
    public static void removeFromTimeOut(Channel channel) {
        ScheduledFuture<?> future = TIMEOUT_TASKS.remove(channel);
        if (future != null) future.cancel(false);
    }

    @Sharable
    private static final class ChannelActiveListener extends ChannelDuplexHandler {

        @Override
        public boolean isSharable() {
            return true;
        }

        private ChannelActiveListener() {
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Channel channel = ctx.channel();
            ChannelPipeline pipeline = channel.pipeline();
        }
    }

/*    @Sharable
    private static final class ExceptionPreventer extends ChannelInboundHandlerAdapter {

        //Maybe 5 second timeout if handshake or login start not delivered within that time?
        //private long time;

        private ExceptionPreventer() {
            //this.time = System.currentTimeMillis();
        }

*//*        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg.getClass() == ReflectionUtils.handshakePacketClass)
                this.time = System.currentTimeMillis();
            if (msg.getClass() == ReflectionUtils.loginInStartPacketClass)
                this.time = System.currentTimeMillis();
            super.channelRead(ctx, msg);
        }*//*

        //https://www.decompiler.com/jar/33130520cc4c4726b53ef8626609b558/XDDOS.jar/XD/XDDOS/methods/impl/InvalidIPSpoof.java
        //https://github.com/AnAverageBeing/XDDOS

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            Main.logError("ERROR: " + cause.getMessage());
            if (exceptionValidator.isInvalid(cause)) {
                Main.logError("MSG: " + cause.getClass().getSimpleName());
                FireWallManager.addCauseException(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
                ctx.channel().close();
            } //else if (cause != ReadTimeoutException.INSTANCE) super.exceptionCaught(ctx, cause);
        }
    }*/

    private static final ByteBuf
            alreadyConnectingPacket = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("already-connecting"));

    private static final AttributeKey<String> JOINED_WITH_IP = AttributeKey.valueOf("alix-joined-with-ip");

    public static void onHandshake(Channel channel, PacketHandshake handshake) {
        channel.attr(JOINED_WITH_IP).set(handshake.getExtractedHost());
    }

    private static final AttributeKey<UUID> SENT_LOGIN_UUID = AttributeKey.valueOf("alix-sent-login-uuid");

    public static void assignLoginUUID(Channel channel, PacketLoginStart loginStart) {
        var uuid = loginStart.getUUID();
        if (uuid != null) channel.attr(SENT_LOGIN_UUID).set(uuid);
    }

    public static UUID getLoginAssignedUUID(Player player) {
        var user = UserManager.get(player.getUniqueId());
        var channel = user.getChannel();
        return getLoginAssignedUUID(channel);
    }

    public static UUID getLoginAssignedUUID(Channel channel) {
        return channel.hasAttr(SENT_LOGIN_UUID) ? channel.attr(SENT_LOGIN_UUID).get() : null;
    }

    private static final String UNKNOWN_IP = Bukkit.getIp();

    @NotNull
    public static String getJoinedWithIP(Channel channel) {
        if (!channel.hasAttr(JOINED_WITH_IP))
            return UNKNOWN_IP;

        var ip = channel.attr(JOINED_WITH_IP).get();
        return ip != null ? ip : UNKNOWN_IP;
    }

    //Thanks onechris ;]
    //https://github.com/onebeastchris/GeyserPackSync/blob/master/common%2Fsrc%2Fmain%2Fjava%2Fnet%2Fonebeastchris%2Fgeyserpacksync%2Fcommon%2Futils%2FFloodgateUtil.java#L12-L15

    //returns true on allowed, false on disallowed login and closed connection
    /*public static boolean onLoginStart(User user, String packetUsername, String serverUsername, PersistentUserData data, boolean deemedPremium) {
        //User user = event.getUser();
        Channel channel = (Channel) user.getChannel();

        removeFromTimeOut(channel);
        if (channel.pipeline().context(CHANNEL_MONITOR_NAME) != null) channel.pipeline().remove(CHANNEL_MONITOR_NAME);

        AlixCommonHandler.getPreLoginVerdict(channel, packetUsername, serverUsername, data, deemedPremium, Dependencies.isBedrock(channel), verdict -> {
            switch (verdict) {
                case ALLOWED:
                    break;
                case DISALLOWED_INVALID_NAME:
                    NettyUtils.closeAfterConstSend(channel, invalidNamePacket);
                    return false;
                case DISALLOWED_PREVENT_FIRST_JOIN:
                    NettyUtils.closeAfterConstSend(channel, preventFirstTimeJoinPacket);
                    return false;
                case DISALLOWED_MAX_ACCOUNTS_REACHED:
                    NettyUtils.closeAfterConstSend(channel, maxTotalAccountsPacket);
                    return false;
                case DISALLOWED_VPN_DETECTED:
                    NettyUtils.closeAfterConstSend(channel, vpnDetectedPacket);
                    return false;
                case DISALLOWED_DIFFERENTLY_CASED_NAME_EXISTS:
                    NettyUtils.closeAfterConstSend(channel, accountExistsUnderDifferentCase);
                    return false;
            }
        });


        //BufPreProcessor.remove(channel);
        *//*else {//it's impossible for a legit client to send this packet twice - assume it's a hacked one, and immediately firewall the ip
            //FireWallManager.add(((InetSocketAddress) channel.remoteAddress()).getAddress(), REASON);
            channel.close();
            return;
        }*//*

        //String serverUsername = LoginInStartGetter.getName((ByteBuf) event.getByteBuf());
        //ChannelWrapper

        //Main.logError("NAME IN LOGIN START: " + serverUsername + " ATTR: " + channel.attr(floodgate_player) + " CHANNEL: " + channel.getClass().getName());

        return putConnecting(user, serverUsername);
    }*/

    public static boolean putConnecting(User user, String name) {
        //Main.debug("putConnecting: '" + name + "'");

        Channel channel = (Channel) user.getChannel();
        user.getProfile().setName(name);//set the user's name prematurely, since it's used for identifying the user on removal
        //user.getProfile().setUUID(new WrapperLoginClientLoginStart(event).getPlayerUUID().get());
        User currentlyConnecting = UserManager.putConnecting(name, user);//get the currently already connecting user (or this very user if he doesn't exist, that being mostly the case) close the connection of the one trying to connect in this very method execution

        //channel.closeFuture().addListener()

        //identity equality check
        //fix for a race condition caused by two players connecting with the same nickname
        if (currentlyConnecting != user) {
            Channel ccChannel = (Channel) currentlyConnecting.getChannel();
            if (AlixCommonUtils.getAddress(channel).equals(AlixCommonUtils.getAddress(ccChannel))) {
                ccChannel.close();
                return true;
            }
            NettyUtils.closeAfterConstSend(channel, alreadyConnectingPacket);
            //event.setCancelled(true);
            return false;
        }
        return true;
    }

    /*private static final ChannelFutureListener REMOVE_CONNECTING = future -> {
        UserManager.removeConnecting()
    };*/

/*    private static final class InvalidLoginInStartWrapper extends WrapperLoginClientLoginStart {

        private InvalidLoginInStartWrapper() {
            super(null, null, null, null);
        }
    }*/

    @Sharable
    private static final class ChannelMonitor extends ChannelInboundHandlerAdapter {

        @Override
        public boolean isSharable() {
            return true;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //cause.printStackTrace();
            //Main.logError("ERROR1: " + cause.getMessage());
            try {
                if (cause instanceof DecoderException) {
                    //UNSAFE.putObject(cause, CAUSE_OFFSET, cause); //A PacketEvents error printing prevention - sets the cause of the exception to itself (which means that no other exception caused this one), which removes the PacketProcessException as the cause
                    FireWallManager.addCauseException(AlixCommonUtils.getAddress(ctx.channel()), cause);
                    ctx.channel().close();
                    return;
                }
            } catch (Exception e) {
                AlixCommonUtils.logException(e);
            }
            super.exceptionCaught(ctx, cause);
        }
    }

    static void init() {
    }

    private AlixChannelHandler() {
    }
}