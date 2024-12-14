package shadow.systems.netty;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.ConnectionThreadManager;
import alix.common.connection.filters.AntiVPN;
import alix.common.connection.filters.ConnectionManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.PersistentUserData;
import alix.common.messages.Messages;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.protocol.player.User;
import alix.libs.com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import alix.libs.io.github.retrooper.packetevents.injector.handlers.PacketEventsDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.DecoderException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.UserManager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static io.netty.channel.ChannelHandler.Sharable;

public final class AlixChannelHandler {

    //public static final Map<String, Channel> CHANNELS = new MapMaker().weakValues().makeMap();//ensure we do not hold the Channel reference captive by using weak values
    private static final Map<Channel, ScheduledFuture<?>> TIMEOUT_TASKS = new ConcurrentHashMap<>();//we fully control this map, so no need for weak reference usage
    public static final String
            CHANNEL_ACTIVE_LISTENER_NAME = "alix-active-listener",
            CHANNEL_MONITOR_NAME = "alix-channel-monitor";
    //PACKET_ANALYSER_NAME = "AlixPacketAnalyser",
    //PACKET_ANALYSER_NAME = "AlixPacketAnalyser";
    private static final ChannelActiveListener CHANNEL_ACTIVE_LISTENER = new ChannelActiveListener();
    private static final ChannelMonitor CHANNEL_MONITOR = new ChannelMonitor();
    //private static final PacketAnalyser packetAnalyser = new PacketAnalyser();
    //private static final ExceptionPreventer EXCEPTION_PREVENTER = new ExceptionPreventer();
    //private static final ExceptionValidator exceptionValidator = ExceptionValidator.createImpl();

    //private static long time;
    //private static final SharedIdleReadListener

    public static void inject(Channel channel) {
        //Main.logError("INJECTED B4 " + channel.pipeline().names() + " " + channel.isRegistered());

        //BufPreProcessor.addPreProcessor(channel);
        channel.pipeline().addLast(CHANNEL_ACTIVE_LISTENER_NAME, CHANNEL_ACTIVE_LISTENER);//wait for channel active, as we cannot inject out packet listener before that, since the server hasn't injected the channel yet
        //Main.logError("INJECTED AFTER " + channel.pipeline().names() + " " + channel.isRegistered());
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

    /*private static final class PacketAnalyser extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
            //NettyUtils.validate(ctx, byteBuf);
            if (byteBuf.readableBytes() != 0) Main.logWarning("ID: " + NettyUtils.readPacketId(byteBuf));
        }
    }*/

    @Sharable
    private static final class ChannelActiveListener extends ChannelDuplexHandler {

        //private final String injectAfter;//, afterDecoder;

        @Override
        public boolean isSharable() {
            return true;
        }

        private ChannelActiveListener() {
            //if (Dependencies.isProtocolLibPresent) Main.logInfo("Fixing a specific ProtocolLib injection problem.");
            //this.injectAfter = PacketEvents.DECODER_NAME; //Dependencies.isProtocolLibPresent ? "protocol_lib_inbound_interceptor" : "packet_handler";
            //this.afterDecoder = Dependencies.isAnyViaPresent ? "via-decoder" : "splitter";
        }

/*        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Main.logError("READ ACTIVE LISTENER " + msg);
            super.channelRead(ctx, msg);
        }*/

        /*        @Override
                public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                    Main.logError("REGISTERED " + ctx.name() + " NAMES:::: " + ctx.pipeline().names());
                    super.channelRegistered(ctx);
                }*/
        private static final ByteBuf timeOutError = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cTimed Out [Alix]");//"§cError TimeOut. If you're seeing this message, report this immediately - AlixSystem");

        /*@Override
        public void channelRegistered(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            ChannelPipeline pipeline = channel.pipeline();

            pipeline.remove(CHANNEL_REGISTERED_LISTENER_NAME);
            pipeline.addBefore(PacketEvents.DECODER_NAME, BUF_PRE_PROCESSOR_NAME, new BufPreProcessor(pipeline));
            pipeline.addBefore("packet_handler", CHANNEL_MONITOR_NAME, CHANNEL_MONITOR);

            pipeline.fireChannelRegistered();
        }*/

        private static final boolean timeout = false;

        //Handler addition and removal can be optimized with UnsafeNettyUtils when their methods are completed
        @OptimizationCandidate
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Channel channel = ctx.channel();
            ChannelPipeline pipeline = channel.pipeline();

            if (AlixUtils.antibotService) {
                pipeline.addBefore("packet_handler", CHANNEL_MONITOR_NAME, CHANNEL_MONITOR);
                PacketEventsDecoder decoder = (PacketEventsDecoder) pipeline.context(PacketEvents.DECODER_NAME).handler();

                //Fix for invalid packet attacks
                //used until automatically removed during handler relocation after SET_COMPRESSION
                pipeline.replace(PacketEvents.DECODER_NAME, PacketEvents.DECODER_NAME, new AlixPEDecoder(decoder));

                if (timeout) TIMEOUT_TASKS.put(channel, channel.eventLoop().schedule(() -> {
                    TIMEOUT_TASKS.remove(channel);
                    NettyUtils.closeAfterConstSend(channel, timeOutError);
                }, 7, TimeUnit.SECONDS));
            }
        }

        /*        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            AlixChannelHandler.removeFromTimeOut(ctx.channel());//important fix
            super.close(ctx, promise);
        }*/

        /*        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);
            Channel channel = ctx.channel();
            Main.logError("REGISTERED ctx: " + ctx.pipeline().names());
            BufPreProcessor.addPreProcessor(ctx.channel()); //ProtocolManager.USERS.get(ctx.pipeline()));
            TIMEOUT_TASKS.put(channel, channel.eventLoop().schedule(() -> {
                //ConnectionThreadManager.onPingRequest(((InetSocketAddress) channel.remoteAddress()).getAddress());
                //Main.logError("wyjebao timeout");
                TIMEOUT_TASKS.remove(channel);
                NettyUtils.closeAfterConstSend(channel, timeOutError);
            }, 3, TimeUnit.SECONDS));
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            //Channel channel = ctx.channel();
            Main.logError("CHANNEL ACTIVE");
            //BufPreProcessor.addPreProcessor(channel);
            //channel.pipeline().addFirst(PACKET_ANALYSER_NAME, packetAnalyser);
            //Main.logError("CHANNEL ACTIVE  " + channel.pipeline().names());
            //channel.pipeline().addAfter(this.injectAfter, CHANNEL_MONITOR_NAME, CHANNEL_MONITOR);
            User user = ProtocolManager.USERS.get(channel.pipeline());

            if (user == null) {
                firewall(ctx, "F1");
                return;
            }

            //Main.logError("CHANNEL ACTIVE  " + channel.pipeline().names());
            //channel.pipeline().addAfter(this.afterDecoder, PACKET_ANALYSER_NAME, new PacketAnalyser());//the exception prevention safety measure
            //channel.pipeline().addAfter("prepender", PACKET_ANALYSER_NAME, EXCEPTION_PREVENTER);
            //channel.pipeline().addLast(PACKET_ANALYSER_NAME, packetAnalyser);//the exception prevention safety measure
            TIMEOUT_TASKS.put(channel, channel.eventLoop().schedule(() -> {
                //ConnectionThreadManager.onPingRequest(((InetSocketAddress) channel.remoteAddress()).getAddress());
                //Main.logError("wyjebao timeout");
                TIMEOUT_TASKS.remove(channel);
                NettyUtils.closeAfterConstSend(channel, timeOutError);
            }, 3, TimeUnit.SECONDS));
            //Main.logWarning("LISTENER ACTIVUH " + ctx.pipeline().names());
            //time = System.currentTimeMillis();
        }

*//*        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            super.handlerAdded(ctx);
            Main.logError("NAME: " + ctx.name() + " " + ctx.pipeline().names());
            if (ctx.name().equals(PacketEvents.DECODER_NAME)) BufPreProcessor.addPreProcessor(ctx.channel());
        }*//*

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            removeFromTimeOut(ctx.channel());//important fix
            super.close(ctx, promise);
        }*/

/*                @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            super.handlerRemoved(ctx);
            Main.logError("HANDLER UNREGISTERED: " + ctx.handler());
        }

                @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            //Main.logError("CHANNEL UNREGISTERED: " + ctx.name());
            ScheduledFuture<?> future = TIMEOUT_TASKS.remove(ctx.channel());
            if (future != null) future.cancel(true);
            super.channelUnregistered(ctx);
        }
                @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.channel().close();
            super.exceptionCaught(ctx, cause);
            //Main.logWarning("LISTENER ACTIVATE ERROR " + cause.toString());
            //cause.printStackTrace();
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            Main.logError("CHANNEL REGISTERED: " + ctx.name());
            super.channelRegistered(ctx);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            Main.logWarning("PRE-TO-ADDED " + ctx.pipeline().names());
            super.handlerAdded(ctx);
            Main.logWarning("ADDED " + ctx.pipeline().names());
        }*/
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
            invalidNamePacket = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("anti-bot-invalid-name-blocked")),
            alreadyConnectingPacket = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("already-connecting")),
            preventFirstTimeJoinPacket = OutDisconnectPacketConstructor.constructConstAtLoginPhase(ConnectionManager.preventFirstTimeJoinMessage),
            maxTotalAccountsPacket = OutDisconnectPacketConstructor.constructConstAtLoginPhase(GeoIPTracker.maxAccountsReached),
            vpnDetectedPacket = OutDisconnectPacketConstructor.constructConstAtLoginPhase(AntiVPN.antiVpnMessage);


    //private static final ByteBuf invalidNamePacket = OutDisconnectKickPacketConstructor.constructConstAtLoginPhase(Messages.get("anti-bot-invalid-name-blocked"));

    //private static final InvalidLoginInStartWrapper INVALID_LOGIN_WRAPPER = new InvalidLoginInStartWrapper();
    //private static final String REASON = "LoginInStart sent twice";

    //Handler addition and removal can be optimized with UnsafeNettyUtils when their methods are completed

    private static final AttributeKey<String> JOINED_WITH_IP = AttributeKey.valueOf("alix-joined-with-ip");

    public static void onHandshake(PacketReceiveEvent event) {
        WrapperHandshakingClientHandshake wrapper = new WrapperHandshakingClientHandshake(event);
        //Main.debug("HANDSHAKE RECEIVED BY BUKKIT SERVER: " + AlixUtils.getFields(wrapper));
        Channel channel = (Channel) event.getChannel();
        channel.attr(JOINED_WITH_IP).set(wrapper.getServerAddress());
    }

    @NotNull
    public static String getJoinedWithIP(Channel channel) {
        return channel.attr(JOINED_WITH_IP).get();
    }

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]*");
    private static final boolean validateName = Main.config.getBoolean("validate-name");

    //Thanks onechris ;]
    //https://github.com/onebeastchris/GeyserPackSync/blob/master/common%2Fsrc%2Fmain%2Fjava%2Fnet%2Fonebeastchris%2Fgeyserpacksync%2Fcommon%2Futils%2FFloodgateUtil.java#L12-L15

    //returns true on allowed, false on disallowed login and closed connection
    public static boolean onLoginStart(User user, String name, PersistentUserData data, boolean deemedPremium) {
        //User user = event.getUser();
        Channel channel = (Channel) user.getChannel();

        removeFromTimeOut(channel);
        if (channel.pipeline().context(CHANNEL_MONITOR_NAME) != null) channel.pipeline().remove(CHANNEL_MONITOR_NAME);

        //BufPreProcessor.remove(channel);
        /*else {//it's impossible for a legit client to send this packet twice - assume it's a hacked one, and immediately firewall the ip
            //FireWallManager.add(((InetSocketAddress) channel.remoteAddress()).getAddress(), REASON);
            channel.close();
            return;
        }*/

        //String name = LoginInStartGetter.getName((ByteBuf) event.getByteBuf());
        //ChannelWrapper

        //Main.logError("NAME IN LOGIN START: " + name + " ATTR: " + channel.attr(floodgate_player) + " CHANNEL: " + channel.getClass().getName());
        if (validateName && (name.length() > 16 || name.isEmpty() || !NAME_PATTERN.matcher(name).matches())) {
            //FireWallManager.add(user.getAddress().getAddress(), "E1");
            NettyUtils.closeAfterConstSend(channel, invalidNamePacket);
            //event.setLastUsedWrapper(INVALID_LOGIN_WRAPPER);//we know it'll throw an exception during a normal read, so if other plugins try to read it they should check for nulls (although they are more than likely to be using a separate PE instance, but not much I can do about that)
            //event.setCancelled(true);
            return false;
        }

        //String name = Dependencies.getCorrectUsername(channel, name); //Dependencies.FLOODGATE_PREFIX != null && Dependencies.isBedrock(channel) ? Dependencies.FLOODGATE_PREFIX + name : name;
        //String name = name;

        //AlixScheduler.async(() -> (?)
        InetAddress address = user.getAddress().getAddress();

        if (AlixUtils.antibotService)
            ConnectionThreadManager.onJoinAttempt(name, address);

        if (data == null) {
            if (!deemedPremium && ConnectionManager.disallowJoin(name)) {//Close the connection before the auth thread is started
                NettyUtils.closeAfterConstSend(channel, preventFirstTimeJoinPacket);
                //event.setCancelled(true);
                return false;
            }

            if (GeoIPTracker.disallowJoin(address, name)) {
                NettyUtils.closeAfterConstSend(channel, maxTotalAccountsPacket);
                //event.setCancelled(true);
                return false;
            }

            if (AntiVPN.disallowJoin(address)) {
                NettyUtils.closeAfterConstSend(channel, vpnDetectedPacket);
                //event.setCancelled(true);
                return false;
            }
        }

        user.getProfile().setName(name);//set the user's name prematurely, since it's used for identifying the user on removal
        //user.getProfile().setUUID(new WrapperLoginClientLoginStart(event).getPlayerUUID().get());
        User cU = UserManager.putConnecting(name, user);//get the currently already connecting user (or this very user if he doesn't exist, that being mostly the case) close the connection of the one trying to connect in this very method execution

        //channel.closeFuture().addListener()

        //identity equality check
        //fix for a race condition caused by two players connecting with the same nickname
        if (cU != user) {
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

/*        private static final String REASON = "LoginInStart sent twice";

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg.getClass() == ReflectionUtils.loginInStartPacketClass) {
                Channel channel = ctx.channel();
                ScheduledFuture<?> task = TIMEOUT_TASKS.remove(channel);
                if (task != null) task.cancel(false);
                else {//it's impossible for a legit client to send this packet twice - assume it's a hacked one, and immediately firewall the ip
                    FireWallManager.add(((InetSocketAddress) channel.remoteAddress()).getAddress(), REASON);
                    channel.close();
                    return;
                }
                //Main.logError("TIME: " + (System.currentTimeMillis() - time) + "ms");
                //CHANNELS.put(LoginInStartPacketGetter.getPlayerName(msg), channel);
            }
            super.channelRead(ctx, msg);
        }*/

        /*@Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Main.logError("MSG: " + msg.getClass().getSimpleName());
            super.channelRead(ctx, msg);
        }*/

        @Override
        public boolean isSharable() {
            return true;
        }

        //private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
        //private static final long CAUSE_OFFSET = AlixUnsafe.objectFieldOffset(Throwable.class, "cause");

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //cause.printStackTrace();
            //Main.logError("ERROR1: " + cause.getMessage());
            if (cause instanceof DecoderException) {
                //UNSAFE.putObject(cause, CAUSE_OFFSET, cause); //A PacketEvents error printing prevention - sets the cause of the exception to itself (which means that no other exception caused this one), which removes the PacketProcessException as the cause
                FireWallManager.addCauseException((InetSocketAddress) ctx.channel().remoteAddress(), cause);
                ctx.channel().close();
                return;
            }
            super.exceptionCaught(ctx, cause);
        }
    }

    static void init() {
    }

    private AlixChannelHandler() {
    }
}