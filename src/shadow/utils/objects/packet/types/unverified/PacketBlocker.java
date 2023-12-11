package shadow.utils.objects.packet.types.unverified;

import alix.common.data.GuiType;
import alix.common.messages.Messages;
import alix.common.scheduler.impl.AlixScheduler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.login.captcha.manager.CountdownTask;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.packet.injector.ChannelInjector;
import shadow.utils.users.offline.UnverifiedUser;

import java.lang.reflect.Method;

public class PacketBlocker extends ChannelDuplexHandler {

    //private static final boolean pingCheck = false;//Main.config.getBoolean("ping-check");
    //public static boolean optimizedPacketBlocker = ReflectionUtils.checkValid("net.minecraft.network.protocol.game.PacketPlayInKeepAlive");
    private static final String
            movementForbiddenCaptcha = Messages.get("movement-forbidden-captcha"),
            packetLimitReached = Messages.get("packet-limit-reached-verification");
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
        for (Method m : clazz.getMethods()) {
            if (!m.getName().equals("toString") && m.getReturnType() == String.class) {
                return m;
            }
        }
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
    private static final ChannelInjector channelInjector = AlixHandler.createChannelInjectorImpl();
    public static final String packetHandlerName = "alixsystem_unv_handler";
    private final Channel channel;
    protected final UnverifiedUser user;
    private final CountdownTask countdownTask;
    //private SchedulerTask loginKickTask;
    private long lastMovementPacket;
    private int movementPacketsUntilKick, totalPacketsUntilKick;

    protected PacketBlocker(UnverifiedUser user) {
        this.user = user;
        this.channel = channelInjector.inject(user.getPlayer(), this, packetHandlerName);
        this.countdownTask = new CountdownTask(this.channel, !initCaptchaTask || user.hasCompletedCaptcha());
    }

    public static Channel getChannel(Player player) {
        try {
            return channelInjector.getChannel(player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startLoginKickTask() {
        this.countdownTask.setToLogin();
        //Simply setting it to null is enough to cancel it (Deprecated)
        //this.loginKickTask = initLoginTask ? AlixScheduler.runLaterSync(this::kickLoginTimePassed, maxLoginTime, TimeUnit.SECONDS) : null;
    }

    //public abstract PingCheck getPingCheck();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (user.hasCompletedCaptcha()) {//during login/register, the captcha verification was passed so we lift the packet limitations
            switch (msg.getClass().getSimpleName()) {
                case "PacketPlayInKeepAlive": //The player will time out without this one
                case "ServerboundKeepAlivePacket"://another possible name of this packet on 1.17+
                    super.channelRead(ctx, msg);
                    return;
                case "ServerboundChatCommandPacket"://The command packet on 1.17+
                    this.processCommand(msg);
                    return;
                case "PacketPlayInChat":
                    if (!serverboundNameVersion) super.channelRead(ctx, msg);
                    return;
            }
            return;
        }

        //during the captcha verification
        this.captchaVerification(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (user.hasCompletedCaptcha()) {
            switch (msg.getClass().getSimpleName()) {
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

        switch (msg.getClass().getSimpleName()) {
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
        super.write(ctx, msg, promise);
    }

    protected final void writeNotOverridden(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    protected final void captchaVerification(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (serverboundNameVersion) captcha_1_17_ver(ctx, msg);
        else captcha_old_ver(ctx, msg);
    }

    private void captcha_1_17_ver(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (msg.getClass().getSimpleName()) {
            case "PacketPlayInPosition"://most common packets
            case "PacketPlayInPositionLook":
            case "PacketPlayInLook":
            case "d":
                long now = System.currentTimeMillis();

                if (now - this.lastMovementPacket > 995) {//the user is standing still
                    movementPacketsUntilKick--;
                    totalPacketsUntilKick--;
                } else if (++movementPacketsUntilKick == maxMovementPackets) {
                    syncedKick(movementForbiddenCaptcha);
                    return;
                }

                this.lastMovementPacket = now;
                break;
            case "ServerboundChatCommandPacket"://The command packet on 1.17+
                this.processCommand(msg);
                break;
            case "PacketPlayInKeepAlive": //The player will time out without this one
            case "ServerboundKeepAlivePacket"://another possible name of this packet on 1.17+
                super.channelRead(ctx, msg);
                return;//exempt keep alive from the total packet count limitation
            //keep alive is exempt because other plugins may use it to measure ping, and keep alive spam will very much likely get the player kicked out automatically, thus the exemption
        }
        if (++totalPacketsUntilKick == maxTotalPackets) syncedKick(packetLimitReached);
    }

    private void captcha_old_ver(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (msg.getClass().getSimpleName()) {
            case "PacketPlayInPosition"://most common packets
            case "PacketPlayInPositionLook":
            case "PacketPlayInLook":
            case "d":
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
            case "PacketPlayInClientCommand":
                super.channelRead(ctx, msg);
                break;
            case "PacketPlayInKeepAlive": //The player will time out without this one
                super.channelRead(ctx, msg);
                return;//exempt keep alive from the total packet count limitation
            //keep alive is exempt because other plugins may use it to measure ping, and keep alive spam will very much likely get the player kicked out automatically, thus the exemption
        }
        if (++totalPacketsUntilKick == maxTotalPackets) syncedKick(packetLimitReached);
    }

    private void processCommand(Object packet) throws Exception {
        String cmd = (String) getStringFromCommandPacketMethod.invoke(packet);
        AlixCommandManager.handleVerificationCommand(cmd.toCharArray(), this.user);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {//ignoring the possibly client-caused exception
        this.syncedKick("§cUncaught exception: " + cause.getMessage());
    }

    protected final void channelReadNotOverridden(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    public static PacketBlocker getPacketBlocker(UnverifiedUser user, GuiType type) {
        if (type == null) return new PacketBlocker(user);
        switch (type) {
            case PIN:
                return new GUIPacketBlocker(user);
            case ANVIL:
                return new AnvilGUIPacketBlocker(user);
            default:
                throw new AssertionError("Invalid: " + type);
        }
    }

    @NotNull
    public final Channel getChannel() {
        return channel;
    }

    @NotNull
    public final CountdownTask getCountdownTask() {
        return countdownTask;
    }

    protected void syncedKick(String reason) {//The packet interceptor is async and bukkit requires the kick to be sync
        AlixScheduler.sync(() -> kick(reason));
    }

    private void kick(String reason) {
        user.getPlayer().kickPlayer(reason);
    }

/*    protected void kickLoginTimePassed() {
        kick(loginTimePassed);
    }*/

/*    public void cancelLoginKickTask() {
        if (this.loginKickTask != null)
            this.loginKickTask.cancel();
    }*/

    public void stop() {
        //this.cancelLoginKickTask(); //only cancel the login task, as the captcha task will be cancelled when the user is removed from the user map
        this.channel.eventLoop().submit(() -> {
            this.channel.pipeline().remove(packetHandlerName);
            return null;
        });
    }

    public void updateBuilder() {
    }

    public static void init() {
    }
}