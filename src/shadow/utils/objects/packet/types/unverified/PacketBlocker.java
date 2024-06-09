package shadow.utils.objects.packet.types.unverified;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.AlixDeque;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisguisedChat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.login.captcha.manager.CountdownTask;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.users.types.UnverifiedUser;

import java.util.Arrays;

public class PacketBlocker implements PacketProcessor {

    //private static final boolean pingCheck = false;//Main.config.getBoolean("ping-check");
    //public static boolean optimizedPacketBlocker = ReflectionUtils.checkValid("net.minecraft.network.protocol.game.PacketPlayInKeepAlive");

    /*private static final String
            movementForbiddenCaptcha = Messages.get("movement-forbidden-captcha"),
            packetLimitReached = Messages.get("packet-limit-reached-verification");*/

    public static final boolean serverboundChatCommandPacketVersion;
    //uncaughtExceptionKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase(AlixUtils.isDebugEnabled ? "§cUncaught exception: Check the console for errors!" : "§cUncaught exception: Enable 'debug' in config.yml if you want to see the error in the console!");

    //loginTimePassed = Messages.get("login-time-passed"),
    protected static final byte
            WAIT_PACKETS_INCREASE = 4,//(byte) (ServerEnvironment.isPaper() ? 0 : 3),
            WAIT_PACKETS_THRESHOLD = 5;//(byte) (WAIT_PACKETS_INCREASE + 5);
    //public static final Class<?> commandPacketClass;
    //public static final Method getStringFromCommandPacketMethod;
    private static final ByteBuf movementForbiddenCaptchaKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(Messages.get("movement-forbidden-captcha")), packetLimitReachedKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(Messages.get("packet-limit-reached-verification"));//,
    //protected static final PacketAffirmator affirmator = AlixHandler.createPacketAffirmatorImpl();
    //private static final PingCheckFactory factory = AlixHandler.createPingCheckFactoryImpl();
    private static final int maxMovementPackets = 120 + AlixUtils.maxLoginTime + (AlixUtils.requireCaptchaVerification ? AlixUtils.maxCaptchaTime : 0);//It's not used whenever captcha verification is disabled, but whatever, it can stay this way for now
    private static final int maxTotalPackets = maxMovementPackets + 80;

    //private static final boolean initLoginTask = maxLoginTime >= 3;
    //private static final boolean initCaptchaTask = AlixUtils.requireCaptchaVerification && AlixUtils.maxCaptchaTime >= 3;
    //private static final boolean initPingCheck = AlixUtils.requirePingCheckVerification;

    static {
        //Method stringFromCommandPacket = null;
        // Class<?> packetClazz = null;
        boolean b;
        try {
            Class.forName("net.minecraft.network.protocol.game.ServerboundChatCommandPacket");
            //stringFromCommandPacket = ReflectionUtils.getStringMethodFromPacketClass(packetClazz);
            b = true;
        } catch (ClassNotFoundException ignored) {//ignored
            b = false;
        }
        serverboundChatCommandPacketVersion = b;
        //commandPacketClass = packetClazz;
        //serverboundNameVersion = stringFromCommandPacket != null;
        //getStringFromCommandPacketMethod = stringFromCommandPacket;
    }

    //private final PingCheck pingCheck;//null only if "initPingCheck" is false
    //private final Channel channel;
    protected final UnverifiedUser user;
    private final AlixDeque<Component> blockedChatMessages;
    private final CountdownTask countdownTask;
    protected byte waitPackets;
    protected boolean packetsSent;
    //protected PacketWrapper<?> lastPlayerInfoUpdate;
    //private SchedulerTask loginKickTask;
    private long lastMovementPacket;
    private int movementPacketsUntilKick, totalPacketsUntilKick;

    PacketBlocker(PacketBlocker previousBlocker) {
        this.user = previousBlocker.user;
        //this.channel = previousBlocker.channel;
        this.countdownTask = previousBlocker.countdownTask;
        this.countdownTask.restartAsLogin(); //restart the countdown
        this.blockedChatMessages = previousBlocker.blockedChatMessages;
        this.waitPackets = previousBlocker.waitPackets;
        this.packetsSent = previousBlocker.packetsSent;
    }

    PacketBlocker(UnverifiedUser user) {
        this.user = user;
        //this.channel = channelInjector.inject(user.getPlayer(), handler, packetHandlerName);
        //this.channel = handler.getChannel();
        this.countdownTask = new CountdownTask(user);
        this.blockedChatMessages = new AlixDeque<>();
        this.packetsSent = false;
    }

    public static PacketBlocker getPacketBlocker(PacketBlocker previousBlocker, LoginType type) {
        switch (type) {
            case COMMAND:
                return new PacketBlocker(previousBlocker);
            case PIN:
                return new GUIPacketBlocker(previousBlocker);
            case ANVIL:
                return new AnvilGUIPacketBlocker(previousBlocker);
            default:
                throw new AlixError("Invalid: " + type);
        }
    }

    //public abstract PingCheck getPingCheck();

    public static PacketBlocker getPacketBlocker(UnverifiedUser user, LoginType type) {
        switch (type) {
            case COMMAND:
                return new PacketBlocker(user);
            case PIN:
                return new GUIPacketBlocker(user);
            case ANVIL:
                return new AnvilGUIPacketBlocker(user);
            default:
                throw new AlixError("Invalid: " + type);
        }
    }

    public static void init() {
    }

    public final void startLoginKickTask() {
        this.countdownTask.restartAsLogin();
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

    protected final void trySpoofPackets() {
        if (!this.packetsSent && ++this.waitPackets >= WAIT_PACKETS_THRESHOLD && (this.packetsSent = true))//6 - at least 5 move packets and one out respawn packet from the server
            this.user.spoofVerificationPackets(); //AlixScheduler.async(user::spoofVerificationPackets);// /\ setting the boolean in a branchless if statement for a very slight performance boost
        //Main.logError("wwwwww " + waitPackets);
    }

    @Override
    public void onPacketReceive(PacketPlayReceiveEvent event) {
        if (!user.hasCompletedCaptcha()) {
            this.onReceiveCaptchaVerification(event);
            return;
        }
        //during login/register, the captcha verification was passed so we lift the packet limitations
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                this.trySpoofPackets();
                event.setCancelled(true);
                return;
            case KEEP_ALIVE://will time out without this one
                return;
            case CHAT_COMMAND:
                this.processCommand(new WrapperPlayClientChatCommand(event).getCommand().toCharArray());
                event.setCancelled(true);
                return;
            case CHAT_MESSAGE:
                if (serverboundChatCommandPacketVersion) break;
                WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
                String c = wrapper.getMessage();
                if (c.isEmpty()) break;
                if (c.charAt(0) == '/') this.processCommand(Arrays.copyOfRange(c.toCharArray(), 1, c.length()));
        }
        event.setCancelled(true);//cancel any other packets
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

    @Override
    public void onPacketSend(PacketPlaySendEvent event) {
        //Main.logError("READ: " + event.getPacketType().name());
        if (!user.hasCompletedCaptcha()) {
            this.onSendCaptchaVerification(event);
            return;
        }
        switch (event.getPacketType()) {
            case CHAT_MESSAGE://From the now deprecated ProtocolAccess.convertPlayerChatToSystemPacket
                WrapperPlayServerChatMessage playerChat = new WrapperPlayServerChatMessage(event);
                //Main.logError("CHAT MSG " + playerChat.getMessage().getChatContent().decorate(TextDecoration.STRIKETHROUGH));
                this.blockedChatMessages.offerLast(playerChat.getMessage().getChatContent());
                event.setCancelled(true);
                break;
            //case PLAYER_CHAT_HEADER:
            case DISGUISED_CHAT:
                //Main.logError("DISGUISED CHAT " + new WrapperPlayServerDisguisedChat(event).getMessage().decorate(TextDecoration.STRIKETHROUGH));
                this.blockedChatMessages.offerLast(new WrapperPlayServerDisguisedChat(event).getMessage().decorate(TextDecoration.STRIKETHROUGH));
                event.setCancelled(true);
                break;
            case SYSTEM_CHAT_MESSAGE:
                //Main.logError("SYSTEM CHAT: " + new WrapperPlayServerSystemChatMessage(event).getMessage().decorate(TextDecoration.STRIKETHROUGH));
                Component component = new WrapperPlayServerSystemChatMessage(event).getMessage();
                //A fix for the "message not delivered" stuff
                if (component instanceof TranslatableComponent && ((TranslatableComponent) component).key().equals("multiplayer.message_not_delivered")) {
                    event.setCancelled(true);
                    return;
                }

                this.blockedChatMessages.offerLast(component);
                event.setCancelled(true);
                break;
            case CHANGE_GAME_STATE:
                switch (new WrapperPlayServerChangeGameState(event).getReason()) {
                    case START_LOADING_CHUNKS:
                        this.waitPackets += WAIT_PACKETS_INCREASE;
                        this.trySpoofPackets();
                        break;
                    case CHANGE_GAME_MODE:
                        event.setCancelled(true);
                }
                break;
            //case "PacketPlayOutGameStateChange":
            case TIME_UPDATE:
            case TITLE://
            case SET_TITLE_SUBTITLE://
            case SET_TITLE_TEXT://
            case SET_TITLE_TIMES://
            case ENTITY_EFFECT:
            case EFFECT:
            case SERVER_DATA:
            case SCOREBOARD_OBJECTIVE:
            case RESET_SCORE:
            case UPDATE_SCORE:
            case DISPLAY_SCOREBOARD:
            case WINDOW_ITEMS:
            case SPAWN_PLAYER:
            case SPAWN_LIVING_ENTITY:
            case SPAWN_ENTITY:
            case ENTITY_RELATIVE_MOVE_AND_ROTATION:
            case ENTITY_RELATIVE_MOVE:
            case ENTITY_METADATA:
            case ENTITY_EQUIPMENT:
            case ENTITY_STATUS:
            case ENTITY_VELOCITY:
            case ENTITY_ANIMATION:
            case ENTITY_MOVEMENT:
            case ENTITY_ROTATION:
            case ENTITY_TELEPORT:
            case ENTITY_HEAD_LOOK:
            case REMOVE_ENTITY_EFFECT:
            case UPDATE_ENTITY_NBT:
            case PLAYER_INFO:
            case PLAYER_INFO_REMOVE:
            case PLAYER_INFO_UPDATE:
                event.setCancelled(true);
                break;
        }
        //if (!event.isCancelled()) Main.logError("READ: " + event.getPacketType().name());
    }

    protected final void onSendCaptchaVerification(PacketPlaySendEvent event) {
        switch (event.getPacketType()) {
            case CHAT_MESSAGE://From the now deprecated ProtocolAccess.convertPlayerChatToSystemPacket
                WrapperPlayServerChatMessage playerChat = new WrapperPlayServerChatMessage(event);
                this.blockedChatMessages.offerLast(playerChat.getMessage().getChatContent());
                event.setCancelled(true);
                break;
            //case PLAYER_CHAT_HEADER:
            case DISGUISED_CHAT:
                this.blockedChatMessages.offerLast(new WrapperPlayServerDisguisedChat(event).getMessage());
                event.setCancelled(true);
                break;
            case SYSTEM_CHAT_MESSAGE:
                Component component = new WrapperPlayServerSystemChatMessage(event).getMessage();
                if (component instanceof TranslatableComponent && ((TranslatableComponent) component).key().equals("multiplayer.message_not_delivered")) {
                    event.setCancelled(true);
                    return;
                }
                this.blockedChatMessages.offerLast(component);
                event.setCancelled(true);
                break;
            case CHANGE_GAME_STATE:
                switch (new WrapperPlayServerChangeGameState(event).getReason()) {
                    case START_LOADING_CHUNKS:
                        this.waitPackets += WAIT_PACKETS_INCREASE;
                        this.trySpoofPackets();
                        break;
                    case CHANGE_GAME_MODE:
                        event.setCancelled(true);
                }
                break;
            //case "PacketPlayOutGameStateChange":
            case TIME_UPDATE:
            case TITLE://
            case SET_TITLE_SUBTITLE://
            case SET_TITLE_TEXT://
            case SET_TITLE_TIMES://
            case ENTITY_EFFECT:
            case EFFECT:
            case SERVER_DATA:
            case SCOREBOARD_OBJECTIVE:
            case RESET_SCORE:
            case UPDATE_SCORE:
            case DISPLAY_SCOREBOARD:
            case WINDOW_ITEMS:
            case SPAWN_PLAYER:
            case SPAWN_LIVING_ENTITY:
            case SPAWN_ENTITY:
            case ENTITY_RELATIVE_MOVE_AND_ROTATION:
            case ENTITY_RELATIVE_MOVE:
            case ENTITY_METADATA:
            case ENTITY_EQUIPMENT:
            case ENTITY_STATUS:
            case ENTITY_VELOCITY:
            case ENTITY_ANIMATION:
            case ENTITY_MOVEMENT:
            case ENTITY_ROTATION:
            case ENTITY_TELEPORT:
            case ENTITY_HEAD_LOOK:
            case REMOVE_ENTITY_EFFECT:
            case UPDATE_ENTITY_NBT:
            case PLAYER_INFO:
            case PLAYER_INFO_REMOVE:
            case PLAYER_INFO_UPDATE:
            case BUNDLE:
                event.setCancelled(true);
                break;
        }
        //if (!event.isCancelled()) Main.logError("READ: " + event.getPacketType().name());
    }

/*    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        MethodProvider.kickAsync(this.user, uncaughtExceptionKickPacket);
        if (AlixUtils.isDebugEnabled) cause.printStackTrace();
    }*/

    protected final void onReceiveCaptchaVerification(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                this.trySpoofPackets();

                long now = System.currentTimeMillis();

                if (now - this.lastMovementPacket > 995) {//the user is standing still
                    movementPacketsUntilKick--;
                    totalPacketsUntilKick--;
                } else if (++movementPacketsUntilKick == maxMovementPackets) {
                    MethodProvider.kickAsync(this.user, movementForbiddenCaptchaKickPacket);
                    //syncedKick(movementForbiddenCaptcha);
                    return;
                }

                this.lastMovementPacket = now;
                break;
            case CHAT_MESSAGE:
                CommandManager.onCaptchaCompletionAttempt(this.user, new WrapperPlayClientChatMessage(event).getMessage().trim());
                break;
/*            case CHAT_COMMAND:
                this.processCommand(new WrapperPlayClientChatCommand(event).getCommand().toCharArray());
                event.setCancelled(true);
                break;*/
            case KEEP_ALIVE:
                return;//exempt keep alive from the total packet count limitation
            //keep alive is exempt because other plugins may use it to measure ping, and keep alive spam will very much likely get the player kicked out automatically, thus the exemption
        }
        event.setCancelled(true);
        if (++totalPacketsUntilKick == maxTotalPackets)
            MethodProvider.kickAsync(this.user, packetLimitReachedKickPacket); //syncedKick(packetLimitReached);
    }

    //we execute it async because the password hashing
    //algorithms could be somewhat heavy
    private void processCommand(char[] cmd) {
        AlixScheduler.async(() -> AlixCommandManager.handleVerificationCommand(cmd, this.user));
    }

    public final void sendBlockedPackets() {
        AlixDeque.forEach(this.user::writeDynamicMessageSilently, this.blockedChatMessages);
        this.user.flushSilently();
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

    //Deprecated: the packet handler is now never
    //removed, only the packet processors change
/*    public void stop() {
        //this.cancelLoginKickTask(); //only cancel the login task, as the captcha task will be cancelled when the user is removed from the user map
        this.channel.eventLoop().submit(() -> {
            this.channel.pipeline().remove(packetHandlerName);
            return null;
        });
    }*/

    @NotNull
    public final CountdownTask getCountdownTask() {
        return countdownTask;
    }

    public void updateBuilder() {
    }

    public static final PacketBlocker EMPTY = new EmptyPacketBlocker();

    private static final class EmptyPacketBlocker extends PacketBlocker {

        private EmptyPacketBlocker() {
            super();
        }

        @Override
        public void onPacketReceive(PacketPlayReceiveEvent event) {
        }

        @Override
        public void onPacketSend(PacketPlaySendEvent event) {
        }
    }

    private PacketBlocker() {
        this.user = null;
        this.blockedChatMessages = null;
        this.countdownTask = null;
    }
}