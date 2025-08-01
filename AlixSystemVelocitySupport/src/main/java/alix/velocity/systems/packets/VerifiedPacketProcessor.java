package alix.velocity.systems.packets;

import alix.common.data.LoginType;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.other.throwable.AlixException;
import alix.velocity.Main;
import alix.velocity.systems.packets.anvil.VerifiedAnvilBuilder;
import alix.velocity.systems.packets.gui.impl.IpAutoLoginGUI;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.connection.login.LoginInfo;
import ua.nanit.limbo.connection.login.gui.AnvilBuilderGoal;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static alix.common.utils.config.ConfigProvider.config;

public final class VerifiedPacketProcessor {

    private static final boolean ipAutoLoginAsk = config.getBoolean("ip-autologin-ask") && !ConfigParams.forcefullyDisableAutoLogin;
    private final VerifiedUser user;
    private final LoginInfo info;
    private CurrentAction currentAction;

    //Password setting
    private Supplier<LoginType> loginType;
    private VerifiedAnvilBuilder builder;

    //Auth proving


    private static final AlixMessage
            joinVerified = Messages.getAsObject("log-player-join-auto-verified"),
            registerJoinMessage = Messages.getAsObject("log-player-join-registered"),
            loginJoinMessage = Messages.getAsObject("log-player-join-logged-in");

    public VerifiedPacketProcessor(VerifiedUser user) {
        this.user = user;
        var channel = user.getChannel();

        var attr = channel.attr(LoginInfo.JOIN_INFO);
        if (attr == null) {
            user.getPlayer().disconnect(Component.text("§cSomething went wrong."));
            this.info = null;
            throw new AlixException("No LoginInfo");
        }
        this.info = attr.get();

        this.sentAutoLoginAsk = !ipAutoLoginAsk || info.joinedRegistered();
        this.currentAction = CurrentAction.NONE;

        this.logJoin();
    }

    private static final String
            autoLoginMessage = Messages.autoLoginMessage,
            autoLoginPremium = Messages.getWithPrefix("auto-login-premium"),
            autoRegisterPremium = Messages.getWithPrefix("auto-register-premium"),
            loginSuccess = Messages.getWithPrefix("login-success");

    private String joinMessage() {
        if (!info.verdict().isAutoLogin())
            return loginSuccess;

        switch (info.verdict()) {
            case IP_AUTO_LOGIN:
                return autoLoginMessage;
            case LOGIN_PREMIUM:
                return autoLoginPremium;
            case REGISTER_PREMIUM:
                return autoRegisterPremium;
            default:
                return "§c<Something went wrong - Could not get join message>";
        }
    }

    private void logJoin() {
        if (info.verdict().isAutoLogin()) {
            Main.logInfo(joinVerified.format(this.user.getName(), this.user.getAddress().getHostAddress(), info.verdict().readableName()));
            return;
        }

        AlixMessage msg = info.joinedRegistered() ? loginJoinMessage : registerJoinMessage;
        Main.logInfo(msg.format(this.user.getName(), this.user.getAddress()));
    }

    public void enablePasswordSetting(Consumer<String> onValidConfirmation, Runnable returnOriginalGui, Supplier<LoginType> loginType) {
        this.currentAction = CurrentAction.SETTING_PASSWORD;
        this.loginType = loginType;
        boolean pin = this.loginType.get() == LoginType.PIN;
        this.builder = new VerifiedAnvilBuilder(this.user, returnOriginalGui, onValidConfirmation, pin ? AnvilBuilderGoal.CHANGE_PIN : AnvilBuilderGoal.CHANGE_PASSWORD);
        this.builder.map();//temporarily switch the used gui
    }

    private void passwordInput(String text) {
        //String text = new WrapperPlayClientNameItem(event).getItemName(); //(String) ReflectionUtils.inItemNamePacketTextMethod.invoke(event);
        //String invalidityReason = AlixCommonUtils.getPasswordInvalidityReason(text, this.loginType.get());

        this.builder.updateText(text);
        this.builder.spoofItems();
        /*this.builder.updateValidity(invalidityReason);

        if (invalidityReason != null) this.builder.spoofItemsInvalidIndicate();
        else this.builder.spoofAllItems();*/
    }

    private boolean sentAutoLoginAsk, sentJoinMessage;
    private byte waitPackets = 5;

    public void onReceive(PacketPlayReceiveEvent event) {
        if (!sentJoinMessage && --waitPackets == 0) {
            if (!sentAutoLoginAsk) {
                IpAutoLoginGUI.add(this.user);
                this.sentAutoLoginAsk = true;
            }
            this.user.getPlayer().sendMessage(Component.text(this.joinMessage()));
            this.sentJoinMessage = true;
        }

        switch (this.currentAction) {
            case SETTING_PASSWORD: {
                switch (event.getPacketType()) {
                    case NAME_ITEM:
                        this.passwordInput(new WrapperPlayClientNameItem(event).getItemName());
                        event.setCancelled(true);
                        return;
                    case CLOSE_WINDOW:
                        this.disablePasswordSetting();
                        event.setCancelled(true);
                        //event.setCancelled(true);
                        return;
                    case CLICK_WINDOW:
                        this.builder.spoofItems();
                        event.setCancelled(true);
                        return;
                    /*case PLUGIN_MESSAGE:
                        WrapperPlayClientPluginMessage wrapper = new WrapperPlayClientPluginMessage(event);
                        if (wrapper.getChannelName().equals("MC|ItemName")) {
                            this.passwordInput(AnvilGUIPacketBlocker.getOldAnvilInput(wrapper.getData()));
                            event.setCancelled(true);
                        }
                        //Bukkit.broadcastMessage("IN: " + wrapper.getChannelName() + " " + Arrays.toString(wrapper.getData()) + " " + new String(wrapper.getData(), StandardCharsets.UTF_8));
                        //event.setCancelled(true);*/
                }
                return;
            }
        }
    }

    private List<ItemStack> items;

    public void onSend(PacketPlaySendEvent event) {
        switch (this.currentAction) {
            case SETTING_PASSWORD: {
                switch (event.getPacketType()) {
                    case WINDOW_ITEMS:
                        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
                        int windowId = packet.getWindowId();
                        if (windowId == 0) this.items = packet.getItems();
                        event.setCancelled(true);
                }
            }
        }
    }

    public void disablePasswordSetting() {
        if (this.items != null)
            this.user.sendPacketSilently(new WrapperPlayServerWindowItems(0, 0, this.items, null));

        this.currentAction = CurrentAction.NONE;
        this.loginType = null;
        this.builder = null;
        this.items = null;
    }

    public void verifyAuthAccess(Runnable actionOnCorrectInput) {
        /*this.currentAction = CurrentAction.VERIFYING_AUTH_ACCESS;
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
        this.authBuilder.openGUI();*/
    }

    private enum CurrentAction {
        NONE, SETTING_PASSWORD, VERIFYING_AUTH_ACCESS
    }
}