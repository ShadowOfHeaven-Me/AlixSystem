package shadow.utils.objects.packet.types.verified;

import alix.common.data.LoginType;
import alix.common.scheduler.AlixScheduler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import shadow.Main;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.systems.gui.AlixGUI;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.gui.builders.AnvilPasswordBuilder;
import shadow.utils.users.User;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class VerifiedPacketProcessor extends PacketProcessor {

    //private static final String packetHandlerName = "alixsystem_ver_handler";
    //private final Channel channel;
    //private final Map<String, Object> map;
    private final User user;
    private boolean settingPassword;
    private AnvilPasswordBuilder builder;
    private Supplier<LoginType> loginType;

    private VerifiedPacketProcessor(User user, PacketInterceptor handler) {
        super(handler);
        this.user = user;
        //this.map = new ConcurrentHashMap<>(16);
        //channel.pipeline().addBefore("packet_handler", packetHandlerName, this);
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (msg.getClass().getSimpleName()) {
            case "ServerboundChatCommandPacket":
                String cmd = (String) PacketBlocker.getStringFromCommandPacketMethod.invoke(msg);
                String[] splet = AlixUtils.split(cmd, ' ');
                //again, async cuz of password hashing algorithms
                if (AlixCommandManager.isPasswordChangeCommand(splet[0]))
                    AlixScheduler.async(() -> CommandManager.onPasswordChangeCommand(user, Arrays.copyOfRange(splet, 1, splet.length)));
                else super.channelRead(ctx, msg);
                return;
            case "PacketPlayInItemName":
                if (!settingPassword) {
                    super.channelRead(ctx, msg);//the user is just renaming an item
                    return;
                }
                this.passwordInput(msg);
                return;
            case "PacketPlayInCloseWindow":
                if (settingPassword) this.disablePasswordSetting();
                super.channelRead(ctx, msg);
                return;
            case "PacketPlayInWindowClick":
                super.channelRead(ctx, msg);
                this.builder.spoofValidAccordingly();
                return;
            default:
                super.channelRead(ctx, msg);
        }
    }

    @Override
    public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (settingPassword && msg.getClass() == ReflectionUtils.outWindowOpenPacketClass) {//Open Inventory
            //Main.logInfo("SERVER: " + settingPassword + " ID: " + ReflectionUtils.outWindowOpenIdMethod.invoke(msg));
            super.write(ctx, msg, promise);
            this.builder.updateWindowID((int) ReflectionUtils.outWindowOpenIdMethod.invoke(msg));
            this.builder.spoofValidAccordingly();
            return;
        }

        super.write(ctx, msg, promise);
    }

    /*if (msg.getClass() != PacketBlocker.commandPacketClass) {
        super.channelRead(ctx, msg);
        return;
    }*/

    private void passwordInput(Object packet) {
        try {
            String text = (String) ReflectionUtils.inItemNamePacketTextMethod.invoke(packet);

            String invalidityReason = AlixUtils.getPasswordInvalidityReason(text, this.loginType.get());

            if (invalidityReason != null) this.builder.spoofItemsInvalidIndicate();
            else this.builder.spoofAllItems();

            this.builder.input(text, invalidityReason);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enablePasswordSetting(Consumer<String> onValidConfirmation, Runnable returnOriginalGui, Supplier<LoginType> loginType) {
        this.settingPassword = true;
        this.loginType = loginType;
        this.builder = new AnvilPasswordBuilder(this.user, this.loginType.get() == LoginType.PIN, onValidConfirmation, returnOriginalGui);
        AlixGUI.MAP.put(user.getUUID(), this.builder);//temporarily switch the used gui
        this.user.getPlayer().openInventory(builder.getGUI());
    }

    public void disablePasswordSetting() {
        this.settingPassword = false;
        this.loginType = null;
        this.builder = null;
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

    public static VerifiedPacketProcessor getProcessor(User user, PacketInterceptor handler) {
        return new VerifiedPacketProcessor(user, handler); //PacketBlocker.serverboundNameVersion ? new VerifiedPacketProcessor(user, handler) : null;
    }
}