package shadow.utils.objects.packet.types.unverified;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.AlixDeque;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.PacketPlayOutWindowItems;
import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.login.captcha.manager.CountdownTask;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.users.offline.UnverifiedUser;

import java.lang.reflect.Method;

public class PacketBlocker extends PacketProcessor {

    //private static final boolean pingCheck = false;//Main.config.getBoolean("ping-check");
    //public static boolean optimizedPacketBlocker = ReflectionUtils.checkValid("net.minecraft.network.protocol.game.PacketPlayInKeepAlive");

    /*private static final String
            movementForbiddenCaptcha = Messages.get("movement-forbidden-captcha"),
            packetLimitReached = Messages.get("packet-limit-reached-verification");*/

    private static final Object
            movementForbiddenCaptchaKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase(Messages.get("movement-forbidden-captcha")),
            packetLimitReachedKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase(Messages.get("packet-limit-reached-verification")),
            uncaughtExceptionKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase(AlixUtils.isDebugEnabled ? "§cUncaught exception: Check the console for errors!" : "§cUncaught exception: Enable 'debug' in config.yml if you want to see the error in the console!");

    //loginTimePassed = Messages.get("login-time-passed"),

    public static final boolean serverboundNameVersion;
    public static final Class<?> commandPacketClass;
    public static final Method getStringFromCommandPacketMethod;

    static {
        Method stringFromCommandPacket = null;
        Class<?> packetClazz = null;
        try {
            packetClazz = Class.forName("net.minecraft.network.protocol.game.ServerboundChatCommandPacket");
            stringFromCommandPacket = getCmd(packetClazz);
        } catch (ClassNotFoundException ignored) {//ignored

        }
        commandPacketClass = packetClazz;
        serverboundNameVersion = stringFromCommandPacket != null;
        getStringFromCommandPacketMethod = stringFromCommandPacket;
    }

    private static Method getCmd(Class<?> clazz) {
        for (Method m : clazz.getMethods())
            if (!m.getName().equals("toString") && m.getReturnType() == String.class)
                return m;
        throw new Error("Class: " + clazz);
    }

    //protected static final PacketAffirmator affirmator = AlixHandler.createPacketAffirmatorImpl();
    //private static final PingCheckFactory factory = AlixHandler.createPingCheckFactoryImpl();
    private static final int maxMovementPackets = 120 + AlixUtils.maxLoginTime + (AlixUtils.requireCaptchaVerification ? AlixUtils.maxCaptchaTime : 0);//It's not used whenever captcha verification is disabled, but whatever, it can stay this way for now
    private static final int maxTotalPackets = maxMovementPackets + 80;

    //private static final boolean initLoginTask = maxLoginTime >= 3;
    private static final boolean initCaptchaTask = AlixUtils.requireCaptchaVerification && AlixUtils.maxCaptchaTime >= 3;
    //private static final boolean initPingCheck = AlixUtils.requirePingCheckVerification;

    //private final PingCheck pingCheck;//null only if "initPingCheck" is false
    private final Channel channel;
    protected final UnverifiedUser user;
    private final CountdownTask countdownTask;
    protected final AlixDeque<Object> blockedChatPackets;
    //private SchedulerTask loginKickTask;
    private long lastMovementPacket;
    private int movementPacketsUntilKick, totalPacketsUntilKick;
    protected static final byte WAIT_PACKETS_INCREASE = 3;
    protected byte waitPackets;
    protected boolean packetsSent;

    protected PacketBlocker(UnverifiedUser user, PacketInterceptor handler) {
        super(handler);
        this.user = user;
        //this.channel = channelInjector.inject(user.getPlayer(), handler, packetHandlerName);
        this.channel = handler.getChannel();
        this.countdownTask = new CountdownTask(user, !initCaptchaTask || user.hasCompletedCaptcha());
        this.blockedChatPackets = new AlixDeque<>();
    }

    public void startLoginKickTask() {
        this.countdownTask.setToLogin();
        //Simply setting it to null is enough to cancel it (Deprecated)
        //this.loginKickTask = initLoginTask ? AlixScheduler.runLaterSync(this::kickLoginTimePassed, maxLoginTime, TimeUnit.SECONDS) : null;
    }

    //public abstract PingCheck getPingCheck();

    protected final void trySpoofPackets() {
        if (!this.packetsSent && ++this.waitPackets >= 8 && (this.packetsSent = true))//8 - at least 5 move packets and one out respawn packet from the server
            this.user.spoofVerificationPackets();//setting the boolean in a branchless if statement for a slight performance boost
        //Main.logError("wwwwww " + waitPackets);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (user.hasCompletedCaptcha()) {//during login/register, the captcha verification was passed so we lift the packet limitations
            switch (msg.getClass().getSimpleName()) {
                case "PacketPlayInPosition"://most common packets
                case "PacketPlayInPositionLook":
                case "PacketPlayInLook":
                case "d":
                    this.trySpoofPackets();
                    return;
                case "PacketPlayInKeepAlive": //The player will time out without this one
                case "ServerboundKeepAlivePacket"://another possible name of this packet on 1.20.2+
                    super.channelRead(ctx, msg);
                    return;
                case "ServerboundChatCommandPacket"://The command packet on 1.17+
                    this.processCommand(msg);
                    return;
                case "PacketPlayInChat":
                    if (!serverboundNameVersion) super.channelRead(ctx, msg);
                    return;
                default:
                    return;
            }
        }

        //during the captcha verification
        //Main.logInfo("CLIENT: " + msg.getClass().getSimpleName());
        this.onReadCaptchaVerification(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //Main.logInfo("SERVER: " + msg.getClass().getSimpleName());
        if (user.hasCompletedCaptcha()) {
            switch (msg.getClass().getSimpleName()) {
                case "PacketPlayOutChat":
                case "ClientboundSystemChatPacket":
                    this.blockedChatPackets.offerLast(msg);
                    return;
                case "PacketPlayOutRespawn":
                case "ClientboundRespawnPacket":
                    this.waitPackets += WAIT_PACKETS_INCREASE;
                    break;
                case "PacketPlayOutGameStateChange":
                case "PacketPlayOutRelEntityMove":
                case "PacketPlayOutNamedEntitySpawn":
                case "PacketPlayOutSpawnEntityLiving":
                case "PacketPlayOutSpawnEntity":
                case "PacketPlayOutEntityMetadata":
                case "PacketPlayOutEntityEquipment":
                case "PacketPlayOutEntityHeadRotation":
                case "PacketPlayOutEntityStatus":
                case "PacketPlayOutEntityVelocity":
                case "PacketPlayOutEntityDestroy":
                case "PacketPlayOutEntityLook":
                case "PacketPlayOutPlayerInfo":
                case "ClientboundPlayerInfoPacket":
                case "ClientboundPlayerInfoUpdatePacket":
                    //Main.logInfo("BLOCKED: " + msg.getClass().getSimpleName());
                    return;
            }
            //Main.logInfo("RECEIVED: " + msg.getClass().getSimpleName());
            super.write(ctx, msg, promise);
            return;
        }
        this.onWriteCaptchaVerification(ctx, msg, promise);
    }

/*    protected final boolean spoofedWindowItems(ChannelHandlerContext context, Object msg, ChannelPromise promise) throws Exception {
        if (msg.getClass() == ReflectionUtils.outWindowItemsPacketClass) {
            if (promise.getClass() == AlixVoidPromise.class) {
                super.write(context, msg, channel.voidPromise());
                return true;
            }
            if (!this.captchaPacketsSent) {
                this.captchaPacketsSent = true;
                Main.logInfo("SEEEEEEEEEEEENT");
                AlixScheduler.async(user::spoofPackets);// this.user.spoofPackets();
                return true;
            }
            return true;
        }
        return false;
    }*/

    protected final void writeNotOverridden(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    protected final void onWriteCaptchaVerification(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        switch (msg.getClass().getSimpleName()) {
            case "PacketPlayOutChat":
            case "ClientboundSystemChatPacket":
                this.blockedChatPackets.offerLast(msg);
                return;
            case "PacketPlayOutRespawn":
            case "ClientboundRespawnPacket":
                this.waitPackets += WAIT_PACKETS_INCREASE;
                break;
            case "PacketPlayOutGameStateChange":
            case "PacketPlayOutWindowItems":
            case "PacketPlayOutRelEntityMove":
            case "PacketPlayOutNamedEntitySpawn":
            case "PacketPlayOutSpawnEntityLiving":
            case "PacketPlayOutSpawnEntity":
            case "PacketPlayOutEntityMetadata":
            case "PacketPlayOutEntityEquipment":
            case "PacketPlayOutEntityHeadRotation":
            case "PacketPlayOutEntityStatus":
            case "PacketPlayOutEntityVelocity":
            case "PacketPlayOutEntityDestroy":
            case "PacketPlayOutEntityLook":
            case "PacketPlayOutPlayerInfo":
            case "ClientboundPlayerInfoPacket":
            case "ClientboundPlayerInfoUpdatePacket":
            case "ClientboundBundlePacket":
                //Main.logInfo("BLOCKED: " + msg.getClass().getSimpleName());
                return;
        }

        //Main.logInfo("RECEIVED: " + msg.getClass().getSimpleName());
        //Main.logInfo("SERVER: " + msg.getClass().getSimpleName());
        super.write(ctx, msg, promise);
    }

    protected final void onReadCaptchaVerification(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (msg.getClass().getSimpleName()) {
            case "PacketPlayInPosition"://most common packets
            case "PacketPlayInPositionLook":
            case "PacketPlayInLook":
            case "d":
                this.trySpoofPackets();

                long now = System.currentTimeMillis();

                if (now - this.lastMovementPacket > 995) {//the user is standing still
                    movementPacketsUntilKick--;
                    totalPacketsUntilKick--;
                } else if (++movementPacketsUntilKick == maxMovementPackets) {
                    MethodProvider.kickAsync(this.channel, movementForbiddenCaptchaKickPacket);
                    //syncedKick(movementForbiddenCaptcha);
                    return;
                }

                this.lastMovementPacket = now;
                break;
            case "PacketPlayInChat":
                if (serverboundNameVersion)
                    break;//don't process chat packets on 1.17+, since commands now have a separate packet
            case "ServerboundChatCommandPacket"://The command packet on 1.17+
                this.processCommand(msg);
                break;
            case "PacketPlayInKeepAlive": //The player will time out without this one
            case "ServerboundKeepAlivePacket"://another possible name of this packet on 1.17+
                super.channelRead(ctx, msg);
                return;//exempt keep alive from the total packet count limitation
            //keep alive is exempt because other plugins may use it to measure ping, and keep alive spam will very much likely get the player kicked out automatically, thus the exemption
        }
        if (++totalPacketsUntilKick == maxTotalPackets)
            MethodProvider.kickAsync(this.channel, packetLimitReachedKickPacket); //syncedKick(packetLimitReached);
    }

    /*private void captcha_old_ver(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (msg.getClass().getSimpleName()) {
            case "PacketPlayInPosition"://most common packets
            case "PacketPlayInPositionLook":
            case "PacketPlayInLook":
            case "d":
                if (!this.captchaPacketsSent && (this.captchaPacketsSent = true)) this.user.spoofPackets();//setting the boolean in a branchless if statement for performance boost

                long now = System.currentTimeMillis();

                if (now - this.lastMovementPacket > 995) {
                    movementPacketsUntilKick--;
                    totalPacketsUntilKick--;
                } else if (++movementPacketsUntilKick == maxMovementPackets) {
                    syncedKick(movementForbiddenCaptcha);
                    return;
                }

                this.lastMovementPacket = now;
                break;
            case "PacketPlayInChat":
            //case "PacketPlayInClientCommand":
                super.channelRead(ctx, msg);
                break;
            case "PacketPlayInKeepAlive": //The player will time out without this one
                super.channelRead(ctx, msg);
                return;//exempt keep alive from the total packet count limitation
            //keep alive is exempt because other plugins may use it to measure ping, and keep alive spam will very much likely get the player kicked out automatically, thus the exemption
        }
        if (++totalPacketsUntilKick == maxTotalPackets) syncedKick(packetLimitReached);
    }*/

    //we execute it async because the password hashing
    //algorithms could be somewhat heavy
    private void processCommand(Object packet) {
        AlixScheduler.async(() -> AlixCommandManager.handleVerificationCommand(getCmd(packet).toCharArray(), this.user));
    }

    private static String getCmd(Object packet) {
        try {
            return (String) getStringFromCommandPacketMethod.invoke(packet);
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        MethodProvider.kickAsync(this.channel, uncaughtExceptionKickPacket);
        if (AlixUtils.isDebugEnabled) cause.printStackTrace();
    }

    protected final void channelReadNotOverridden(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    public static PacketBlocker getPacketBlocker(UnverifiedUser user, LoginType type, PacketInterceptor handler) {
        switch (type) {
            case COMMAND:
                return new PacketBlocker(user, handler);
            case PIN:
                return new GUIPacketBlocker(user, handler);
            case ANVIL:
                return new AnvilGUIPacketBlocker(user, handler);
            default:
                throw new AlixError("Invalid: " + type);
        }
    }

    @NotNull
    public final CountdownTask getCountdownTask() {
        return countdownTask;
    }

/*    protected void syncedKick(String reason) {
        AlixScheduler.sync(() -> user.getPlayer().kickPlayer(reason));
    }*/

/*    protected void kickLoginTimePassed() {
        kick(loginTimePassed);
    }*/

/*    public void cancelLoginKickTask() {
        if (this.loginKickTask != null)
            this.loginKickTask.cancel();
    }*/

    public AlixDeque<Object> getBlockedChatPackets() {
        return blockedChatPackets;
    }

    public Channel getChannel() {
        return channel;
    }

    //Deprecated: the packet handler is now never
    //removed, only the packet processors change
/*    public void stop() {
        //this.cancelLoginKickTask(); //only cancel the login task, as the captcha task will be cancelled when the user is removed from the user map
        this.channel.eventLoop().submit(() -> {
            this.channel.pipeline().remove(packetHandlerName);
            return null;
        });
    }*/

    public void updateBuilder() {
    }

    public static void init() {
    }
}