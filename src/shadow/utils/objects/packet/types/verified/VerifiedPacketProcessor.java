package shadow.utils.objects.packet.types.verified;

import alix.common.data.LoginType;
import alix.common.scheduler.AlixScheduler;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.netty.buffer.ByteBuf;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.gui.AlixGUI;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.gui.builders.AnvilPasswordBuilder;
import shadow.utils.users.types.VerifiedUser;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class VerifiedPacketProcessor implements PacketProcessor {

    //private static final String packetHandlerName = "alixsystem_ver_handler";
    //private final Channel channel;
    //private final Map<String, Object> map;
    private final VerifiedUser user;
    private boolean settingPassword;
    private AnvilPasswordBuilder builder;
    private Supplier<LoginType> loginType;
    private ByteBuf lastItemsPacket;

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
                this.passwordInput(event);
                event.setCancelled(true);
                return;
            case CLOSE_WINDOW:
                this.disablePasswordSetting();
                event.setCancelled(true);
                return;
            case CLICK_WINDOW:
                this.builder.spoofValidAccordingly();
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
        if (!settingPassword) return;

        switch (event.getPacketType()) {
            case OPEN_WINDOW:
                this.builder.updateWindowId(new WrapperPlayServerOpenWindow(event).getContainerId());
                return;
            case WINDOW_ITEMS:
                this.builder.updateWindowId(new WrapperPlayServerWindowItems(event).getWindowId());
                if (this.lastItemsPacket != null) this.lastItemsPacket.release();
                this.lastItemsPacket = ((ByteBuf) event.getByteBuf()).copy();
                event.setCancelled(true);
        }
    }

    /*if (msg.getClass() != PacketBlocker.commandPacketClass) {
        super.channelRead(ctx, msg);
        return;
    }*/

    private void passwordInput(PacketPlayReceiveEvent event) {
        String text = new WrapperPlayClientNameItem(event).getItemName(); //(String) ReflectionUtils.inItemNamePacketTextMethod.invoke(event);

        String invalidityReason = AlixUtils.getPasswordInvalidityReason(text, this.loginType.get());

        if (invalidityReason != null) this.builder.spoofItemsInvalidIndicate();
        else this.builder.spoofAllItems();

        this.builder.input(text, invalidityReason);
    }

    public void enablePasswordSetting(Consumer<String> onValidConfirmation, Runnable returnOriginalGui, Supplier<LoginType> loginType) {
        this.settingPassword = true;
        this.loginType = loginType;
        this.builder = new AnvilPasswordBuilder(this.user, this.loginType.get() == LoginType.PIN, onValidConfirmation, returnOriginalGui);
        AlixGUI.MAP.put(user.getUUID(), this.builder);//temporarily switch the used gui
        this.user.getPlayer().openInventory(builder.getGUI());
    }

    public void disablePasswordSetting() {
        if (this.lastItemsPacket != null) user.silentContext().writeAndFlush(this.lastItemsPacket);
        this.settingPassword = false;
        this.loginType = null;
        this.builder = null;
        this.lastItemsPacket = null;
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