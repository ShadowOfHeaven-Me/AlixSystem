package shadow.utils.objects.packet.types.verified;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.AlixDeque;
import alix.libs.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.*;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.*;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Location;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.gui.AlixGUI;
import shadow.systems.login.auth.GoogleAuth;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.types.unverified.AnvilGUIPacketBlocker;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.gui.builders.BukkitAnvilPasswordBuilder;
import shadow.utils.objects.savable.data.gui.builders.auth.VerifiedVirtualAuthBuilder;
import shadow.utils.objects.savable.data.gui.builders.auth.VirtualAuthBuilder;
import shadow.utils.users.types.VerifiedUser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class VerifiedPacketProcessor implements PacketProcessor {

    //private static final String packetHandlerName = "alixsystem_ver_handler";
    //private final Channel channel;
    //private final Map<String, Object> map;
    private final VerifiedUser user;
    private CurrentAction currentAction;

    //password setting
    private BukkitAnvilPasswordBuilder builder;
    private Supplier<LoginType> loginType;
    private List<ItemStack> items;

    //qr code show
    private final AlixDeque<Component> blockedChatMessages;
    private long lastMovementPacket, lastTeleport;
    private VirtualAuthBuilder authBuilder;
    //might be non-thread-safe
    //@ScheduledForFix
    //private AuthReminder authReminder;

    //private int windowId;

    private VerifiedPacketProcessor(VerifiedUser user) {
        this.user = user;
        this.currentAction = CurrentAction.NONE;
        this.blockedChatMessages = new AlixDeque<>();
        //this.map = new ConcurrentHashMap<>(16);
        //channel.pipeline().addBefore("packet_handler", packetHandlerName, this);
    }

    @Override
    public void onPacketReceive(PacketPlayReceiveEvent event) {
        switch (this.currentAction) {
            case NONE: {
                switch (event.getPacketType()) {
                    case CHAT_COMMAND:
                        this.processCommand(new WrapperPlayClientChatCommand(event).getCommand(), event);
                        return;
                    case CHAT_MESSAGE:
                        if (PacketBlocker.serverboundChatCommandPacketVersion) break;
                        WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
                        String c = wrapper.getMessage();
                        if (c.isEmpty()) break;
                        if (c.charAt(0) == '/') this.processCommand(c.substring(1), event);
                        return;
                }
                return;
            }

            case SETTING_PASSWORD: {
                switch (event.getPacketType()) {
                    case NAME_ITEM:
                        this.passwordInput(new WrapperPlayClientNameItem(event).getItemName());
                        event.setCancelled(true);
                        return;
                    case CLOSE_WINDOW:
                        this.disablePasswordSetting();
                        //event.setCancelled(true);
                        return;
                    case CLICK_WINDOW:
                        this.builder.spoofValidAccordingly();
                        return;
                    case PLUGIN_MESSAGE:
                        WrapperPlayClientPluginMessage wrapper = new WrapperPlayClientPluginMessage(event);
                        if (wrapper.getChannelName().equals("MC|ItemName")) {
                            this.passwordInput(AnvilGUIPacketBlocker.getOldAnvilInput(wrapper.getData()));
                            event.setCancelled(true);
                        }
                        //Bukkit.broadcastMessage("IN: " + wrapper.getChannelName() + " " + Arrays.toString(wrapper.getData()) + " " + new String(wrapper.getData(), StandardCharsets.UTF_8));
                        //event.setCancelled(true);
                }
                return;
            }
            case VERIFYING_AUTH_ACCESS: {
                //Main.logError("PACKET: " + event.getPacketType());
                switch (event.getPacketType()) {
                    case CLICK_WINDOW://the item spoofing happens here \/
                        this.authBuilder.select(new WrapperPlayClientClickWindow(event).getSlot());
                        break;
                    case CLOSE_WINDOW:
                        this.endQRCodeShowAndTeleportBack();
                        return;
                    case KEEP_ALIVE:
                        return;
                }
                event.setCancelled(true);
                return;
            }
            case VIEWING_QR_CODE: {
                //Main.logError("PACKET: " + event.getPacketType());
                switch (event.getPacketType()) {
                    case PLAYER_POSITION://most common packets
                    case PLAYER_POSITION_AND_ROTATION:
                    case PLAYER_ROTATION:
                    case PLAYER_FLYING:

                        long now = System.currentTimeMillis();
                        //the user is not standing still, spoof teleport
                        if (now - this.lastMovementPacket < 995 && now - lastTeleport > 250) {
                            this.user.writeAndFlushConstSilently(GoogleAuth.QR_LOC_TELEPORT);
                            this.lastTeleport = now;//the player sends a position and rotation packet after each teleport - prevent packet spam with a tp limit
                        }

                        this.lastMovementPacket = now;
                        break;
                    case CHAT_COMMAND:
                        this.processChat(new WrapperPlayClientChatCommand(event).getCommand());
                        break;
                    case CHAT_COMMAND_UNSIGNED:
                        this.processChat(new WrapperPlayClientChatCommandUnsigned(event).getCommand());
                        break;
                    case KEEP_ALIVE:
                        return;
                }
                event.setCancelled(true);
            }
        }
    }


    private static final ByteBuf
            authCancelMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("google-auth-setting-cancel-chat"));

    public void verifyAuthAccess(Runnable actionOnCorrectInput) {
        this.currentAction = CurrentAction.VERIFYING_AUTH_ACCESS;
        this.authBuilder = new VerifiedVirtualAuthBuilder(this.user, correct -> {
            if (correct) {
                VerifiedVirtualAuthBuilder.visualsOnProvenAccess(this.user);
                this.user.getData().getLoginParams().setHasProvenAuthAccess(true);
                this.endQRCodeShowAndTeleportBack();
                if (actionOnCorrectInput != null) actionOnCorrectInput.run();
                return;
            }
            VerifiedVirtualAuthBuilder.visualsOnDeniedAccess(this.user);
        });
        this.authBuilder.openGUI();
    }

    private void processChat(String chat) {
        //Main.logError("CHAT " + chat);
        switch (chat) {
            case "confirm": {
                //init the gui
                if (!this.user.getData().getLoginParams().hasProvenAuthAccess()) this.verifyAuthAccess(null);
                else this.endQRCodeShowAndTeleportBack();
                return;
            }
            case "cancel": {
                this.user.writeAndFlushConstSilently(authCancelMessagePacket);
                this.endQRCodeShowAndTeleportBack();
            }
        }
    }

    private void endQRCodeShowAndTeleportBack() {
        this.endQRCodeShow();
        Location original = this.user.originalLocation.get();
        if (original != null) {
            MethodProvider.teleportAsync(this.user.getPlayer(), original);
            this.user.originalLocation.set(null);//let's not do a lazySet here
        }
    }

    //String cmd = (String) PacketBlocker.getStringFromCommandPacketMethod.invoke(msg);
    private void processCommand(String cmd, PacketReceiveEvent event) {
        String[] splet = AlixUtils.split(cmd, ' ');
        //again, async cuz of password hashing algorithms
        if (AlixCommandManager.isPasswordChangeCommand(splet[0])) {
            AlixScheduler.async(() -> CommandManager.onPasswordChangeCommand(user, Arrays.copyOfRange(splet, 1, splet.length)));
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketSend(PacketPlaySendEvent event) {

/*        switch (event.getPacketType()) {
            case PLAYER_INFO: {
                WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo(event);
                List<WrapperPlayServerPlayerInfo.PlayerData> list = new ArrayList<>(info.getPlayerDataList().size());//since the List in the packet can not support modifications
                for (WrapperPlayServerPlayerInfo.PlayerData data : info.getPlayerDataList()) {
                    Player p = Bukkit.getPlayer(data.getUser().getUUID());
                    if (OutPlayerInfoPacketConstructor.isVisible(this.user.getPlayer(), p)) list.add(data);
                }

                if (list.size() != info.getPlayerDataList().size()) {
                    info.setPlayerDataList(list);
                    event.markForReEncode(true);
                }
                return;
            }
            case PLAYER_INFO_UPDATE: {
                WrapperPlayServerPlayerInfoUpdate info = new WrapperPlayServerPlayerInfoUpdate(event);
                List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> list = new ArrayList<>(info.getEntries().size());//since the List in the packet can not support modifications
                for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo data : info.getEntries()) {
                    Player p = Bukkit.getPlayer(data.getGameProfile().getUUID());
                    if (OutPlayerInfoPacketConstructor.isVisible(this.user.getPlayer(), p)) list.add(data);
                }

                if (list.size() != info.getEntries().size()) {
                    info.setEntries(list);
                    event.markForReEncode(true);
                }
                return;
            }
        }*/

        switch (this.currentAction) {
            case SETTING_PASSWORD: {
                switch (event.getPacketType()) {
                    case OPEN_WINDOW:
                        this.builder.updateWindowId(new WrapperPlayServerOpenWindow(event).getContainerId());
                        return;
                    case WINDOW_ITEMS:
                        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
                        int windowId = packet.getWindowId();
                        this.builder.updateWindowId(windowId);
                        if (windowId == 0) this.items = packet.getItems();
                        event.setCancelled(true);
                }
                return;
            }

            case VERIFYING_AUTH_ACCESS: {
                switch (event.getPacketType()) {
                    case CHAT_MESSAGE://From the now deprecated ProtocolAccess.convertPlayerChatToSystemPacket
                        WrapperPlayServerChatMessage playerChat = new WrapperPlayServerChatMessage(event);
                        this.blockedChatMessages.offerLast(playerChat.getMessage().getChatContent());
                        event.setCancelled(true);
                        return;
                    //case PLAYER_CHAT_HEADER:
                    case DISGUISED_CHAT:
                        this.blockedChatMessages.offerLast(new WrapperPlayServerDisguisedChat(event).getMessage());
                        event.setCancelled(true);
                        return;
                    case SYSTEM_CHAT_MESSAGE:
                        event.setCancelled(true);
                        WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
                        if (wrapper.isOverlay()) return;//action bar

                        Component component = wrapper.getMessage();
                        if (component instanceof TranslatableComponent && ((TranslatableComponent) component).key().equals("multiplayer.message_not_delivered")) {
                            return;
                        }
                        this.blockedChatMessages.offerLast(component);
                        return;
                    case WINDOW_ITEMS:
                        event.setCancelled(true);
                        return;
                }
            }
            case VIEWING_QR_CODE: {
                switch (event.getPacketType()) {
                    case CHAT_MESSAGE://From the now deprecated ProtocolAccess.convertPlayerChatToSystemPacket
                        WrapperPlayServerChatMessage playerChat = new WrapperPlayServerChatMessage(event);
                        this.blockedChatMessages.offerLast(playerChat.getMessage().getChatContent());
                        event.setCancelled(true);
                        return;
                    //case PLAYER_CHAT_HEADER:
                    case DISGUISED_CHAT:
                        this.blockedChatMessages.offerLast(new WrapperPlayServerDisguisedChat(event).getMessage());
                        event.setCancelled(true);
                        return;
                    case SYSTEM_CHAT_MESSAGE:
                        event.setCancelled(true);
                        WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
                        if (wrapper.isOverlay()) return;//action bar

                        Component component = wrapper.getMessage();
                        if (component instanceof TranslatableComponent && ((TranslatableComponent) component).key().equals("multiplayer.message_not_delivered")) {
                            return;
                        }
                        this.blockedChatMessages.offerLast(component);
                        return;
                    case CHANGE_GAME_STATE:
                        if (new WrapperPlayServerChangeGameState(event).getReason() != WrapperPlayServerChangeGameState.Reason.START_LOADING_CHUNKS)
                            event.setCancelled(true);
                        return;
                    case WINDOW_ITEMS:
                    case SPAWN_PLAYER:
                    case TITLE://
                    case SET_TITLE_SUBTITLE://
                    case SET_TITLE_TEXT://
                    case SET_TITLE_TIMES://
                    case EFFECT:
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
                        event.setCancelled(true);
                }
            }
        }
    }

    /*if (msg.getClass() != PacketBlocker.commandPacketClass) {
        super.channelRead(ctx, msg);
        return;
    }*/

    private void passwordInput(String text) {
        //String text = new WrapperPlayClientNameItem(event).getItemName(); //(String) ReflectionUtils.inItemNamePacketTextMethod.invoke(event);
        String invalidityReason = AlixUtils.getPasswordInvalidityReason(text, this.loginType.get());

        if (!this.builder.input(text)) return;
        this.builder.updateValidity(invalidityReason);

        if (invalidityReason != null) this.builder.spoofItemsInvalidIndicate();
        else this.builder.spoofAllItems();
    }

    public void enablePasswordSetting(Consumer<String> onValidConfirmation, Runnable returnOriginalGui, Supplier<LoginType> loginType) {
        this.currentAction = CurrentAction.SETTING_PASSWORD;
        this.loginType = loginType;
        this.builder = new BukkitAnvilPasswordBuilder(this.user, this.loginType.get() == LoginType.PIN, onValidConfirmation, returnOriginalGui);
        AlixGUI.MAP.put(user.getUUID(), this.builder);//temporarily switch the used gui
        this.user.getPlayer().openInventory(this.builder.getGUI());
    }

    public void disablePasswordSetting() {
        if (this.items != null)
            this.user.writeAndFlushDynamicSilently(new WrapperPlayServerWindowItems(0, 0, this.items, null));
        //this.windowId = 0;
        this.currentAction = CurrentAction.NONE;
        this.loginType = null;
        this.builder = null;
        this.items = null;
    }

    public void onQuit() {
        //if (authReminder != null) this.authReminder.cancel();
    }

    private Boolean collidableOriginally;

    public void startQRCodeShow() {
        this.currentAction = CurrentAction.VIEWING_QR_CODE;
        this.collidableOriginally = this.user.getPlayer().isCollidable();
        this.user.getPlayer().setCollidable(false);
        //this.authReminder = AuthReminder.reminderFor(this.user);
    }

    public void endQRCodeShow() {
        this.currentAction = CurrentAction.NONE;
        if (collidableOriginally != null) this.user.getPlayer().setCollidable(collidableOriginally);
        this.authBuilder = null;
        this.collidableOriginally = null;
        //this.authReminder.cancel();
        //this.authReminder = null;
        this.blockedChatMessages.forEach(this.user::writeDynamicMessageSilently);
        this.blockedChatMessages.clear();
        this.user.flush();
    }

    /*if (msg.getClass() == ReflectionUtils.outPlayerInfoPacketClass) {
            //this.decode(msg);
            return;
        }*/

/*    private void decode(Object packet) {
        try {
            List<?> playerActionList = (List<?>) ReflectionUtils.getPlayerInfoDataListMethod.invoke(packet);
            Object playerAction = playerActionList.get(0);
            GameProfile profile = (GameProfile) ReflectionUtils.getProfileFromPlayerInfoDataMethod.invoke(playerAction);
            String name = profile.getName();
            if (!LoginVerdictManager.getNullable(name).getVerdict().isAutoLogin())
                this.map.put(name, packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

/*    public void sendOf(String name) {
        Object packet = this.map.get(name);
        if (packet != null)//excluding the self packet send
            this.channel.writeAndFlush(packet);
    }*/

/*    public void stop() {
        this.channel.eventLoop().submit(() -> {
            this.channel.pipeline().remove(packetHandlerName);
            return null;
        });
    }*/

    private enum CurrentAction {

        NONE, SETTING_PASSWORD, VIEWING_QR_CODE, VERIFYING_AUTH_ACCESS

    }

    public static VerifiedPacketProcessor getProcessor(VerifiedUser user) {
        return new VerifiedPacketProcessor(user); //PacketBlocker.serverboundNameVersion ? new VerifiedPacketProcessor(user, handler) : null;
    }
}