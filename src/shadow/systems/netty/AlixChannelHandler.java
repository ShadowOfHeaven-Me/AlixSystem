package shadow.systems.netty;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.Messages;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.concurrent.ScheduledFuture;
import shadow.Main;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.netty.exception.ExceptionValidator;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.holders.packet.getters.LoginInStartGetter;
import shadow.utils.main.AlixUnsafe;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.UserManager;
import sun.misc.Unsafe;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.ChannelHandler.Sharable;

public final class AlixChannelHandler {

    //public static final Map<String, Channel> CHANNELS = new MapMaker().weakValues().makeMap();//ensure we do not hold the Channel reference captive by using weak values
    private static final Map<Channel, ScheduledFuture<?>> TIMEOUT_TASKS = new ConcurrentHashMap<>();//we fully control this map, so no need for weak reference usage
    private static final String
            CHANNEL_ACTIVE_LISTENER_NAME = "alix-active-listener",
            PACKET_INJECTOR_NAME = "AlixChannelInjectListener";//,
    //PACKET_ANALYSER_NAME = "AlixPacketAnalyser",
    //PACKET_ANALYSER_NAME = "AlixPacketAnalyser";
    private static final ChannelActiveListener channelActiveListener = new ChannelActiveListener();
    private static final PacketMonitor packetMonitor = new PacketMonitor();
    //private static final PacketAnalyser packetAnalyser = new PacketAnalyser();
    //private static final ExceptionPreventer EXCEPTION_PREVENTER = new ExceptionPreventer();
    private static final ExceptionValidator exceptionValidator = ExceptionValidator.createImpl();
    //private static long time;
    //private static final SharedIdleReadListener

    public static void inject(Channel channel) {
        channel.pipeline().addFirst(CHANNEL_ACTIVE_LISTENER_NAME, channelActiveListener);//wait for channel active, as we cannot inject out packet listener before that, since the server hasn't injected the channel yet
        //Main.logError("INJECTED  " + channel.pipeline().names());
    }

    public static void uninject(ChannelPipeline pipeline) {
        //Main.logWarning("OPEN  " + pipeline.channel().isOpen());

        pipeline.remove(CHANNEL_ACTIVE_LISTENER_NAME);//unregisters itself for some reason
        pipeline.remove(PACKET_INJECTOR_NAME);//same as this mfo

        //pipeline.remove(PACKET_ANALYSER_NAME);
    }

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

        private static final ByteBuf timeOutError = OutDisconnectKickPacketConstructor.constructConstAtLoginPhase("§cError TimeOut. If you're seeing this message, report this immediately - AlixSystem");
        private final String injectBefore;//, afterDecoder;

        private ChannelActiveListener() {
            if (Dependencies.isProtocolLibPresent) Main.logInfo("Fixing a specific ProtocolLib injection problem.");
            this.injectBefore = Dependencies.isProtocolLibPresent ? "protocol_lib_inbound_interceptor" : "packet_handler";
            //this.afterDecoder = Dependencies.isAnyViaPresent ? "via-decoder" : "splitter";
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Channel channel = ctx.channel();
            //channel.pipeline().addFirst(PACKET_ANALYSER_NAME, packetAnalyser);
            //Main.logError("CHANNEL ACTIVE  " + channel.pipeline().names());
            channel.pipeline().addBefore(this.injectBefore, PACKET_INJECTOR_NAME, packetMonitor);//now we can add the temporary packet listener
            //channel.pipeline().addAfter(this.afterDecoder, PACKET_ANALYSER_NAME, new PacketAnalyser());//the exception prevention safety measure
            //channel.pipeline().addAfter("prepender", PACKET_ANALYSER_NAME, EXCEPTION_PREVENTER);
            //channel.pipeline().addLast(PACKET_ANALYSER_NAME, packetAnalyser);//the exception prevention safety measure
            TIMEOUT_TASKS.put(channel, channel.eventLoop().schedule(() -> {
                //ConnectionThreadManager.onPingRequest(((InetSocketAddress) channel.remoteAddress()).getAddress());
                Main.logError("wyjebao timeout");
                TIMEOUT_TASKS.remove(channel);
                NettyUtils.closeAfterConstSend(channel, timeOutError);
            }, 3, TimeUnit.SECONDS));
            //Main.logWarning("LISTENER ACTIVUH " + ctx.pipeline().names());
            //time = System.currentTimeMillis();
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            removeFromTimeOut(ctx.channel());
            super.close(ctx, promise);
        }

        /*        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            super.handlerRemoved(ctx);
            Main.logError("HANDLER UNREGISTERED: " + ctx.handler());
        }*/

        /*        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            //Main.logError("CHANNEL UNREGISTERED: " + ctx.name());
            ScheduledFuture<?> future = TIMEOUT_TASKS.remove(ctx.channel());
            if (future != null) future.cancel(true);
            super.channelUnregistered(ctx);
        }*/
        /*        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.channel().close();
            super.exceptionCaught(ctx, cause);
            //Main.logWarning("LISTENER ACTIVATE ERROR " + cause.toString());
            //cause.printStackTrace();
        }*/

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
            invalidNamePacket = OutDisconnectKickPacketConstructor.constructConstAtLoginPhase(Messages.get("anti-bot-invalid-name-blocked")),
            alreadyConnectingPacket = OutDisconnectKickPacketConstructor.constructConstAtLoginPhase(Messages.get("already-connecting"));
    //private static final ByteBuf invalidNamePacket = OutDisconnectKickPacketConstructor.constructConstAtLoginPhase(Messages.get("anti-bot-invalid-name-blocked"));

    private static final InvalidLoginInStartWrapper INVALID_LOGIN_WRAPPER = new InvalidLoginInStartWrapper();
    //private static final String REASON = "LoginInStart sent twice";

    public static void onLoginStart(PacketReceiveEvent event) {
        User user = event.getUser();
        Channel channel = (Channel) user.getChannel();
        removeFromTimeOut(channel);
        /*else {//it's impossible for a legit client to send this packet twice - assume it's a hacked one, and immediately firewall the ip
            //FireWallManager.add(((InetSocketAddress) channel.remoteAddress()).getAddress(), REASON);
            channel.close();
            return;
        }*/
        String name = LoginInStartGetter.getName(event);
        //Main.logError("NAME: " + name);
        if (name == null) {
            NettyUtils.closeAfterConstSend(channel, invalidNamePacket);
            event.setLastUsedWrapper(INVALID_LOGIN_WRAPPER);//we know it'll throw an exception during a normal read, so if other plugins try to read it they should check for nulls (although they are more than likely to be using a separate PE instance, but not much I can do about that)
            event.setCancelled(true);//cancel to indicate to others to not process this packet
            return;
        }
        user.getProfile().setName(name);//set the user's name prematurely, since I 
        User cU = UserManager.putConnecting(name, user);//get the currently already connecting user (or this very user if he doesn't exist, that being mostly the case) close the connection of the one trying to connect in this very method execution
        //identity equality check
        //fix for a race condition caused by two players connecting with the same nickname
        if (cU != user) NettyUtils.closeAfterConstSend(channel, alreadyConnectingPacket);
    }

    private static final class InvalidLoginInStartWrapper extends WrapperLoginClientLoginStart {

        private InvalidLoginInStartWrapper() {
            super(null, null, null, null);
        }
    }

    @Sharable
    private static final class PacketMonitor extends ChannelInboundHandlerAdapter {

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

        //private static final JavaLangAccess javaLangAccess = SharedSecrets.getJavaLangAccess();
        private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
        private static final long CAUSE_OFFSET = AlixUnsafe.objectFieldOffset(Throwable.class, "cause");

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (exceptionValidator.isInvalid(cause)) {
                //Main.logError("WYJEBAO ERROR: " + cause.getMessage());
                //cause.printStackTrace();

                //Main.logError("MSG: " + cause.getClass().getSimpleName() + " " + cause.getCause());
                UNSAFE.putObject(cause, CAUSE_OFFSET, cause); //A PacketEvents error printing prevention - sets the cause of the exception to itself, which removes the PacketProcessException as the cause
                //Main.logError("AFTER SET: " + cause.getClass().getSimpleName() + " " + cause.getCause());
                FireWallManager.addCauseException(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
                ctx.channel().close();
                return;
            }// else if (cause != ReadTimeoutException.INSTANCE) super.exceptionCaught(ctx, cause);
            super.exceptionCaught(ctx, cause);
        }
    }

    static void init() {
    }

    private AlixChannelHandler() {
    }
}