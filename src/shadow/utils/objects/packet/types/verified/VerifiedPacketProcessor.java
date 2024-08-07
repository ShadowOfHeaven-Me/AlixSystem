package shadow.utils.objects.packet.types.verified;

import alix.common.data.LoginType;
import alix.common.scheduler.AlixScheduler;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.gui.AlixGUI;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.constructors.OutPlayerInfoPacketConstructor;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.types.unverified.AnvilGUIPacketBlocker;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.gui.builders.BukkitAnvilPasswordBuilder;
import shadow.utils.users.types.VerifiedUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class VerifiedPacketProcessor implements PacketProcessor {

    //private static final String packetHandlerName = "alixsystem_ver_handler";
    //private final Channel channel;
    //private final Map<String, Object> map;
    private final VerifiedUser user;
    private boolean settingPassword;
    private BukkitAnvilPasswordBuilder builder;
    private Supplier<LoginType> loginType;
    private List<ItemStack> items;
    //private int windowId;

    private VerifiedPacketProcessor(VerifiedUser user) {
        this.user = user;
        //this.map = new ConcurrentHashMap<>(16);
        //channel.pipeline().addBefore("packet_handler", packetHandlerName, this);
    }

    @Override
    public void onPacketReceive(PacketPlayReceiveEvent event) {
        if (!settingPassword) {
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
        if (!settingPassword) return;

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
    }

    /*if (msg.getClass() != PacketBlocker.commandPacketClass) {
        super.channelRead(ctx, msg);
        return;
    }*/

    private void passwordInput(String text) {
        //String text = new WrapperPlayClientNameItem(event).getItemName(); //(String) ReflectionUtils.inItemNamePacketTextMethod.invoke(event);

        String invalidityReason = AlixUtils.getPasswordInvalidityReason(text, this.loginType.get());

        if (invalidityReason != null) this.builder.spoofItemsInvalidIndicate();
        else this.builder.spoofAllItems();

        this.builder.input(text, invalidityReason);
    }

    public void enablePasswordSetting(Consumer<String> onValidConfirmation, Runnable returnOriginalGui, Supplier<LoginType> loginType) {
        this.settingPassword = true;
        this.loginType = loginType;
        this.builder = new BukkitAnvilPasswordBuilder(this.user, this.loginType.get() == LoginType.PIN, onValidConfirmation, returnOriginalGui);
        AlixGUI.MAP.put(user.getUUID(), this.builder);//temporarily switch the used gui
        this.user.getPlayer().openInventory(builder.getGUI());
    }

    public void disablePasswordSetting() {
        if (this.items != null)
            this.user.writeAndFlushDynamicSilently(new WrapperPlayServerWindowItems(0, 0, this.items, null));
        //this.windowId = 0;
        this.settingPassword = false;
        this.loginType = null;
        this.builder = null;
        this.items = null;
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

    public static VerifiedPacketProcessor getProcessor(VerifiedUser user) {
        return new VerifiedPacketProcessor(user); //PacketBlocker.serverboundNameVersion ? new VerifiedPacketProcessor(user, handler) : null;
    }
}