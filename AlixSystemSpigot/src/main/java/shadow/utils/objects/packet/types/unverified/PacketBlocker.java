package shadow.utils.objects.packet.types.unverified;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.network.AlixNetworkDeque;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisguisedChat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.login.captcha.consumer.CaptchaConsumer;
import shadow.systems.login.captcha.manager.VirtualCountdown;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.check.fall.VirtualFallPhase;
import shadow.utils.objects.packet.map.BlockedPacketsQueue;
import shadow.utils.users.types.TemporaryUser;
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
    /*protected static final byte
            WAIT_PACKETS_INCREASE = 4,//(byte) (ServerEnvironment.isPaper() ? 0 : 3),
            WAIT_PACKETS_THRESHOLD = 5;//(byte) (WAIT_PACKETS_INCREASE + 5);*/
    //public static final Class<?> commandPacketClass;
    //public static final Method getStringFromCommandPacketMethod;
    private static final ByteBuf movementForbiddenCaptchaKickPacket = OutDisconnectPacketConstructor.constructConstAtPlayPhase(Messages.get("movement-forbidden-captcha")), packetLimitReachedKickPacket = OutDisconnectPacketConstructor.constructConstAtPlayPhase(Messages.get("packet-limit-reached-verification"));//,
    //protected static final PacketAffirmator affirmator = AlixHandler.createPacketAffirmatorImpl();
    //private static final PingCheckFactory factory = AlixHandler.createPingCheckFactoryImpl();
    private static final int maxMovementPackets = 120 + AlixUtils.maxLoginTime + (AlixUtils.requireCaptchaVerification ? AlixUtils.maxCaptchaTime : 0);//It's not used whenever captcha verification is disabled, but whatever, it can stay this way for now
    private static final int correctingTpMovementPackets = 80;
    private static final int maxTotalPackets = maxMovementPackets + 100;

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
    final UnverifiedUser user;
    private final CaptchaConsumer captchaConsumer;

    private final AlixNetworkDeque<Component> blockedChatMessages;
    private final BlockedPacketsQueue packetMap;

    private final VirtualCountdown virtualCountdown;
    final VirtualFallPhase virtualFallPhase;
    //protected PacketWrapper<?> lastPlayerInfoUpdate;
    //private SchedulerTask loginKickTask;
    private long lastMovementPacket;
    private int movementPackets, totalPacketsUntilKick;

    PacketBlocker(PacketBlocker previousBlocker) {
        this.user = previousBlocker.user;
        //this.channel = previousBlocker.channel;
        this.virtualCountdown = previousBlocker.virtualCountdown;
        this.virtualCountdown.restartAsLogin(); //restart the countdown
        this.blockedChatMessages = previousBlocker.blockedChatMessages;
        this.virtualFallPhase = previousBlocker.virtualFallPhase;
        this.packetMap = previousBlocker.packetMap;
        this.captchaConsumer = previousBlocker.captchaConsumer;
    }

    PacketBlocker(UnverifiedUser user, TemporaryUser tempUser) {
        this.user = user;
        //this.channel = channelInjector.inject(user.getPlayer(), handler, packetHandlerName);
        //this.channel = handler.getChannel();
        this.virtualCountdown = new VirtualCountdown(user);
        this.virtualFallPhase = new VirtualFallPhase(user);
        this.blockedChatMessages = tempUser.getUnverifiedProcessor().blockedChatMessages;
        this.packetMap = tempUser.getUnverifiedProcessor().packetMap;
        this.captchaConsumer = CaptchaConsumer.adequateFor(this.user);

        //Should I replace this with an object reference?
        if (tempUser.getUnverifiedProcessor().chunkSent) this.virtualFallPhase.setChunkSent();
    }

    public static PacketBlocker getPacketBlocker(PacketBlocker previousBlocker, LoginType type) {
        if (previousBlocker.user.isBedrock()) return new BedrockPacketBlocker(previousBlocker);
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

    public static PacketBlocker getPacketBlocker(UnverifiedUser user, TemporaryUser tempUser, LoginType type) {
        if (user.isBedrock()) return new BedrockPacketBlocker(user, tempUser);
        switch (type) {
            case COMMAND:
                return new PacketBlocker(user, tempUser);
            case PIN:
                return new GUIPacketBlocker(user, tempUser);
            case ANVIL:
                return new AnvilGUIPacketBlocker(user, tempUser);
            default:
                throw new AlixError("Invalid: " + type);
        }
    }

    public static PacketBlocker getPacketBlocker2FA(UnverifiedUser user, TemporaryUser tempUser) {
        return new AuthGUIPacketBlocker(user, tempUser);
    }

    public static PacketBlocker getPacketBlocker2FA(PacketBlocker previousBlocker) {
        return new AuthGUIPacketBlocker(previousBlocker);
    }

    public static void init() {
    }

    public final void startLoginKickTask() {
        this.virtualCountdown.restartAsLogin();
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

    void onReceive0(PacketPlayReceiveEvent event) {
        //during login/register, the captcha verification was passed so we lift the packet limitations
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                this.virtualFallPhase.trySpoofPackets(event);
                event.setCancelled(true);
                return;
            case TELEPORT_CONFIRM:
                this.virtualFallPhase.tpConfirm(event);
                break;
            case PLUGIN_MESSAGE:
            case PONG:
            case KEEP_ALIVE://will time out without this one
                return;
            case CHAT_COMMAND_UNSIGNED:
                this.processCommand(new WrapperPlayClientChatCommandUnsigned(event).getCommand().toCharArray());
                event.setCancelled(true);
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

    @Override
    public final void onPacketReceive(PacketPlayReceiveEvent event) {
        this.virtualFallPhase.playPhase();
        if (!this.user.hasCompletedCaptcha()) {
            this.onReceiveCaptchaVerification(event);
            return;
        }
        this.onReceive0(event);
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

    public static final boolean filterAllEntityPackets = Main.config.getBoolean("filter-all-entity-packets");

    @Override
    public void onPacketSend(PacketPlaySendEvent event) {
        //Main.logError("READ: " + event.getPacketType().name());
        if (!user.hasCompletedCaptcha()) {
            this.onSendCaptchaVerification(event);
            return;
        }
        switch (event.getPacketType()) {
            case CHUNK_DATA:
            case CHUNK_BATCH_BEGIN:
            case CHUNK_BIOMES:
                this.virtualFallPhase.setChunkSent();
                return;
            case CHAT_MESSAGE://From the now deprecated ProtocolAccess.convertPlayerChatToSystemPacket
                WrapperPlayServerChatMessage playerChat = new WrapperPlayServerChatMessage(event);
                //Main.logError("CHAT MSG " + playerChat.getMessage().getChatContent().decorate(TextDecoration.STRIKETHROUGH));
                this.blockedChatMessages.offerLast(playerChat.getMessage().getChatContent());
                event.setCancelled(true);
                return;
            //case PLAYER_CHAT_HEADER:
            case DISGUISED_CHAT:
                //Main.logError("DISGUISED CHAT " + new WrapperPlayServerDisguisedChat(event).getMessage().decorate(TextDecoration.STRIKETHROUGH));
                this.blockedChatMessages.offerLast(new WrapperPlayServerDisguisedChat(event).getMessage());
                event.setCancelled(true);
                return;
            case SYSTEM_CHAT_MESSAGE:
                //Main.logError("SYSTEM CHAT: " + new WrapperPlayServerSystemChatMessage(event).getMessage().decorate(TextDecoration.STRIKETHROUGH));
                WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
                if (wrapper.isOverlay()) {//action bar
                    event.setCancelled(true);
                    return;
                }
                Component component = wrapper.getMessage();
                //A fix for the "message not delivered" stuff
                if (component instanceof TranslatableComponent && ((TranslatableComponent) component).key().equals("multiplayer.message_not_delivered")) {
                    event.setCancelled(true);
                    return;
                }

                this.blockedChatMessages.offerLast(component);
                event.setCancelled(true);
                return;
            case CHANGE_GAME_STATE:
                if (new WrapperPlayServerChangeGameState(event).getReason() != WrapperPlayServerChangeGameState.Reason.START_LOADING_CHUNKS)
                    event.setCancelled(true);
                return;
/*            case DECLARE_COMMANDS:
                WrapperPlayServerDeclareCommands commands = new WrapperPlayServerDeclareCommands(event);

                break;*/
            //case "PacketPlayOutGameStateChange":

            /*case PLAYER_POSITION_AND_LOOK:
            case UPDATE_VIEW_POSITION:
            case SPAWN_POSITION:*/
            case PLAYER_ABILITIES:
            case UPDATE_ATTRIBUTES:
            case DEATH_COMBAT_EVENT://fix for death
            case UPDATE_HEALTH://fix for death and damage
            case SPAWN_PLAYER:
                //case TIME_UPDATE:
            case TITLE://
            case SET_TITLE_SUBTITLE://
            case SET_TITLE_TEXT://
            case SET_TITLE_TIMES://
            case EFFECT:
            case SET_EXPERIENCE:
            case EXPLOSION:
                //case SERVER_DATA:
            case WINDOW_ITEMS:
            case SET_SLOT:
            case DECLARE_COMMANDS:
                event.setCancelled(true);
                return;
            case BUNDLE:
            case SOUND_EFFECT:
            case ENTITY_SOUND_EFFECT:
            case NAMED_SOUND_EFFECT:
            case SET_PASSENGERS:
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
            case ENTITY_EFFECT:
            case DESTROY_ENTITIES:
                if (filterAllEntityPackets) {//this is enough, as a player won't be shown as long as a tab packet with him isn't sent
                    event.setCancelled(true);
                    return;
                }
                return;
            case PLAYER_INFO:
            case PLAYER_INFO_REMOVE:
            case PLAYER_INFO_UPDATE:
            case RESET_SCORE:
            case UPDATE_SCORE:
            case SCOREBOARD_OBJECTIVE:
            case DISPLAY_SCOREBOARD:
                this.packetMap.addDynamic(event);
                event.setCancelled(true);
        }
        //if (!event.isCancelled()) Main.logError("READ: " + event.getPacketType().name());
    }


    protected final void onSendCaptchaVerification(PacketPlaySendEvent event) {
        switch (event.getPacketType()) {
            case UPDATE_ATTRIBUTES: {
                /*this.user.writeAndFlushDynamicSilently(new WrapperPlayServerUpdateAttributes(user.getPlayer().getEntityId(),
                        Collections.singletonList(new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_MOVEMENT_SPEED, -1,
                                Collections.singletonList(new WrapperPlayServerUpdateAttributes.PropertyModifier(UUID.randomUUID(), -1, WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION))))));
                */
                event.setCancelled(true);
                return;
            }
            case CHUNK_DATA:
            case CHUNK_BATCH_BEGIN:
            case CHUNK_BIOMES:
                this.virtualFallPhase.setChunkSent();
                return;
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
                event.setCancelled(true);
                WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
                if (wrapper.isOverlay()) return;//action bar

                Component component = wrapper.getMessage();
                if (component instanceof TranslatableComponent && ((TranslatableComponent) component).key().equals("multiplayer.message_not_delivered")) {
                    return;
                }
                this.blockedChatMessages.offerLast(component);
                break;
            case CHANGE_GAME_STATE:
                if (new WrapperPlayServerChangeGameState(event).getReason() != WrapperPlayServerChangeGameState.Reason.START_LOADING_CHUNKS)
                    event.setCancelled(true);
                break;
            case PLAYER_ABILITIES:
            case DEATH_COMBAT_EVENT://fix for death
            case UPDATE_HEALTH://fix for death and damage
            case SET_EXPERIENCE:
            case EXPLOSION:
            case SPAWN_PLAYER:
                //case TIME_UPDATE:
            case TITLE:
            case SET_TITLE_SUBTITLE:
            case SET_TITLE_TEXT:
            case SET_TITLE_TIMES:
            case EFFECT:
                //case SERVER_DATA:
            case WINDOW_ITEMS:
            case SET_SLOT:
            case DECLARE_COMMANDS:
                event.setCancelled(true);
                return;
            case BUNDLE:
            case SOUND_EFFECT:
            case ENTITY_SOUND_EFFECT:
            case NAMED_SOUND_EFFECT:
            case SET_PASSENGERS:
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
            case ENTITY_EFFECT:
            case DESTROY_ENTITIES:
                if (filterAllEntityPackets) {
                    event.setCancelled(true);
                    return;
                }
                return;
            case PLAYER_INFO:
            case PLAYER_INFO_REMOVE:
            case PLAYER_INFO_UPDATE:
            case UPDATE_SCORE:
            case RESET_SCORE:
            case SCOREBOARD_OBJECTIVE:
            case DISPLAY_SCOREBOARD:
                this.packetMap.addDynamic(event);
                event.setCancelled(true);
        }
        //if (!event.isCancelled()) Main.logError("READ: " + event.getPacketType().name());
    }

/*    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        MethodProvider.kickAsync(this.user, uncaughtExceptionKickPacket);
        if (AlixUtils.isDebugEnabled) cause.printStackTrace();
    }*/

    private static final ByteBuf movementForbiddenCaptchaChat = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("movement-forbidden-captcha-chat"));

    private void onReceiveCaptchaVerification(PacketPlayReceiveEvent event) {
        //Main.logInfo(event.getPacketType() + " ");
        //user.sendDynamicMessageSilently(event.getPacketType().name());
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                //Main.logInfo("CLIENT: " + event.getPacketType() + " POS: " + new WrapperPlayClientPlayerFlying(event).getLocation());
                if (captchaConsumer != null) this.captchaConsumer.onMove(event);
                this.virtualFallPhase.trySpoofPackets(event);
                if (this.virtualFallPhase.isOngoing()) {
                    event.setCancelled(true);
                    return;
                }

                long now = System.currentTimeMillis();

/*                if (this.movementPackets == correctingTpMovementPackets) {
                    this.user.writeConstSilently(movementForbiddenCaptchaChat);
                    this.virtualFallPhase.tpPosCorrect();
                }*/

                if (now - this.lastMovementPacket > 995) {//the user is standing still
                    this.movementPackets--;
                    this.totalPacketsUntilKick--;
                } else if (++movementPackets == maxMovementPackets) {
                    MethodProvider.kickAsync(this.user, movementForbiddenCaptchaKickPacket);
                    //syncedKick(movementForbiddenCaptcha);
                    break;
                }

                this.lastMovementPacket = now;
                break;
            case TELEPORT_CONFIRM:
                this.virtualFallPhase.tpConfirm(event);
                break;
            case INTERACT_ENTITY:
                if (captchaConsumer != null)
                    this.captchaConsumer.onClick();//new WrapperPlayClientInteractEntity(event).getTarget().orElse(null)
                //this.user.sendDynamicMessageSilently("CURSOR: " + wrapper.getCursorPosition() + " " + wrapper.getBlockPosition());
                break;
            case PLAYER_BLOCK_PLACEMENT:
                //WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
                if (captchaConsumer != null)
                    this.captchaConsumer.onClick();//new WrapperPlayClientPlayerBlockPlacement(event).getCursorPosition()
                //this.user.sendDynamicMessageSilently("CURSOR: " + wrapper.getCursorPosition() + " " + wrapper.getBlockPosition());
                break;
            case CHAT_MESSAGE:
                CommandManager.onCaptchaCompletionAttempt(this.user, new WrapperPlayClientChatMessage(event).getMessage().trim());
                break;
            case ANIMATION:
                //3.7.1 - remove
                //this.user.armSwingReceived = true;
                break;
/*            case CHAT_COMMAND:
                this.processCommand(new WrapperPlayClientChatCommand(event).getCommand().toCharArray());
                event.setCancelled(true);
                break;*/
            case KEEP_ALIVE:
                if (user.keepAliveReceived) return;
                WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event);
                if (keepAlive.getId() == AlixHandler.KEEP_ALIVE_ID) {
                    event.setCancelled(true);

                    this.user.keepAliveReceived = true;
                    //3.7.1 - remove
                    /*if (!this.user.armSwingReceived)
                        MethodProvider.kickAsync(this.user, invalidProtocolError);*/
                }
                return;//exempt keep alive from the total packet count limitation
            case PONG:
            case PLUGIN_MESSAGE:
                return;//exempt plugin message from the total packet count limitation

            //keep alive is exempt because other plugins may use it to measure ping, and keep alive spam will very much likely get the player kicked out automatically, thus the exemption
        }
        event.setCancelled(true);
        if (++totalPacketsUntilKick == maxTotalPackets)
            MethodProvider.kickAsync(this.user, packetLimitReachedKickPacket);
    }

    //long t;

    private static final ByteBuf invalidProtocolError = OutDisconnectPacketConstructor.constructConstAtPlayPhase("§cInvalid Protocol [Alix]");

    //we execute it async because the password hashing
    //algorithms could be somewhat heavy
    private void processCommand(char[] cmd) {
        AlixScheduler.async(() -> AlixCommandManager.handleVerificationCommand(cmd, this.user));
    }

    public final void sendBlockedPackets() {
        this.blockedChatMessages.forEach(this.user::writeDynamicMessageSilently);
        this.packetMap.writeTo(this.user);
        this.user.flush();
    }

    public final void releaseBlocked() {
        this.packetMap.release();
    }

    @NotNull
    public final VirtualCountdown getCountdown() {
        return virtualCountdown;
    }

    @NotNull
    public VirtualFallPhase getFallPhase() {
        return virtualFallPhase;
    }

    public void updateBuilder() {
    }

    public static final PacketBlocker EMPTY = new EmptyPacketBlocker();

    private static final class EmptyPacketBlocker extends PacketBlocker {

        private EmptyPacketBlocker() {
            super();
        }

        @Override
        void onReceive0(PacketPlayReceiveEvent event) {
        }

        @Override
        public void onPacketSend(PacketPlaySendEvent event) {
        }
    }

    private PacketBlocker() {
        this.user = null;
        this.blockedChatMessages = null;
        this.virtualCountdown = null;
        this.captchaConsumer = null;
        this.packetMap = null;
        this.virtualFallPhase = null;
    }
}