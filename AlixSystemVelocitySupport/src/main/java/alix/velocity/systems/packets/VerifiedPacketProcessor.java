package alix.velocity.systems.packets;

import alix.common.data.LoginType;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.packets.message.MessageWrapper;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.other.throwable.AlixException;
import alix.velocity.Main;
import alix.velocity.systems.packets.anvil.VerifiedAnvilBuilder;
import alix.velocity.systems.packets.gui.VelocityAuthBuilder;
import alix.velocity.systems.packets.gui.impl.GoogleAuthGUI;
import alix.velocity.systems.packets.gui.impl.IpAutoLoginGUI;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import ua.nanit.limbo.connection.login.LoginInfo;
import ua.nanit.limbo.connection.login.gui.AnvilBuilderGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static alix.common.utils.config.ConfigProvider.config;

public final class VerifiedPacketProcessor {

    private static final boolean ipAutoLoginAsk = config.getBoolean("ip-autologin-ask") && !ConfigParams.forcefullyDisableAutoLogin;
    private final VerifiedUser user;
    private final LoginInfo loginInfo;
    private CurrentAction currentAction;

    //Password setting
    private Supplier<LoginType> loginType;
    private VerifiedAnvilBuilder builder;

    //Auth proving
    private VelocityAuthBuilder authBuilder;


    private static final AlixMessage
            joinVerified = Messages.getAsObject("log-player-join-auto-verified"),
            registerJoinMessage = Messages.getAsObject("log-player-join-registered"),
            loginJoinMessage = Messages.getAsObject("log-player-join-logged-in");

    public VerifiedPacketProcessor(VerifiedUser user) {
        this.user = user;
        var channel = user.getChannel();

        var attr = channel.hasAttr(LoginInfo.JOIN_INFO) ? channel.attr(LoginInfo.JOIN_INFO) : null;
        if (attr == null) {
            user.getPlayer().disconnect(MessageWrapper.parseLegacy(Messages.getWithPrefix("error-generic")));
            //this.info = null;
            throw new AlixException("No LoginInfo for player " + user.getName());
        }
        this.loginInfo = attr.get();

        this.sentAutoLoginAsk = !ipAutoLoginAsk || loginInfo.joinedRegistered();
        this.currentAction = CurrentAction.NONE;

        this.logJoin();
    }

    private static final String
            autoLoginMessage = Messages.autoLoginMessage,
            autoLoginPremium = Messages.getWithPrefix("auto-login-premium"),
            autoRegisterPremium = Messages.getWithPrefix("auto-register-premium"),
            loginSuccess = Messages.getWithPrefix("login-success");

    private String joinMessage() {
        if (!loginInfo.verdict().isAutoLogin())
            return loginSuccess;

        switch (loginInfo.verdict()) {
            case IP_AUTO_LOGIN:
                return autoLoginMessage;
            case LOGIN_PREMIUM:
                return autoLoginPremium;
            case REGISTER_PREMIUM:
                return autoRegisterPremium;
            default:
                return Messages.getWithPrefix("error-generic");
        }
    }

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    private void logJoin() {
        if (loginInfo.verdict().isAutoLogin()) {
            Main.logInfo(joinVerified.format(this.user.getName(), AlixCommonUtils.getAddress(this.user.getChannel()).getHostAddress(), loginInfo.verdict().readableName()));
            return;
        }

        AlixMessage msg = loginInfo.joinedRegistered() ? loginJoinMessage : registerJoinMessage;
        Main.logInfo(msg.format(this.user.getName(), AlixCommonUtils.getAddress(this.user.getChannel()).getHostAddress()));
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
            this.user.getPlayer().sendMessage(MessageWrapper.parseLegacy(this.joinMessage()));
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
            case VERIFYING_AUTH_ACCESS: {
                //Main.logError("PACKET: " + event.getPacketType());
                switch (event.getPacketType()) {
                    case CLICK_WINDOW://the item spoofing happens here \/
                        this.authBuilder.select(new WrapperPlayClientClickWindow(event).getSlot());
                        event.setCancelled(true);
                        break;
                    case CLOSE_WINDOW:
                        this.authBuilder.onCloseAttempt();
                        return;
                    case KEEP_ALIVE:
                        return;
                }
                //Missing return let any OTHER packet type (e.g. CHAT_COMMAND) fall through into VIEWING_QR's
                //case below and reach processChat() - letting an attacker mid-guess against
                //VelocityAuthBuilder's TOTP attempt cap send "confirm" in chat to spawn a brand-new
                //authBuilder with a freshly reset counter, defeating the cap entirely. See the Spigot
                //original this was ported from (VerifiedPacketProcessor#onReceive), which never falls
                //through here.
                return;
            }
            case VIEWING_QR: {
                switch (event.getPacketType()) {
                    case CHAT_COMMAND:
                        this.processChat(new WrapperPlayClientChatCommand(event).getCommand());
                        event.setCancelled(true);
                        break;
                    case CHAT_COMMAND_UNSIGNED:
                        this.processChat(new WrapperPlayClientChatCommandUnsigned(event).getCommand());
                        event.setCancelled(true);
                        break;
                }
            }
        }
    }

    private static final String
            authCancelMessagePacket = Messages.getWithPrefix("google-auth-setting-cancel-chat");

    //Arms a one-shot action to run once the player, while currently viewing a freshly-shown QR code, types
    //"confirm" in chat and then enters the code their app just generated for it correctly. Used by
    //AuthDataChanges to only actually apply an auth-type change that newly requires the app once the
    //player has proven they scanned the QR right - see the Spigot VerifiedPacketProcessor's equivalent for
    //the full reasoning. Cleared on "cancel" too, so a canceled QR view never leaves a stale action to fire
    //on some later, unrelated "confirm".
    private Runnable pendingQRConfirmAction;

    public void confirmQRCodeThenRun(Runnable action) {
        this.pendingQRConfirmAction = action;
    }

    private void processChat(String chat) {
        switch (chat) {
            case "confirm": {
                //init the gui
                if (!this.user.getData().getLoginParams().hasProvenAuthAccess()) {
                    Runnable pending = this.pendingQRConfirmAction;
                    this.pendingQRConfirmAction = null;
                    //grantsProvenAccess = (pending != null) - see verifyAuthAccess()'s docs above.
                    this.verifyAuthAccess(pending, pending != null);
                } else this.endQRCodeShow();
                return;
            }
            case "cancel": {
                this.user.sendMessage(authCancelMessagePacket);
                this.endQRCodeShow();
            }
        }
    }

    private List<ItemStack> items;

    //The exact item showQrCode() fakes into the off-hand slot - kept here too (not just built inline in
    //GoogleAuthGUI) so onSend() below can splice it into a passing WINDOW_ITEMS resync without needing a
    //round-trip back into that class.
    private static final ItemStack FAKE_QR_MAP_ITEM = ItemStack.builder().type(ItemTypes.FILLED_MAP).amount(1).component(ComponentTypes.MAP_ID, 0).build();

    //The player's REAL off-hand item (window 0, slot QR_MAP_SLOT), as last reported by the actual backend
    //server - tracked passively (see onSend() below) so endQRCodeShow() can restore exactly what was really
    //there instead of just blanking it to air, which would otherwise hide whatever the player was genuinely
    //holding (a shield, totem, etc.) for the rest of their session until some unrelated packet happened to
    //correct it. Defaults to empty, matching a freshly-joined player's actual off-hand.
    private ItemStack realOffHandItem = ItemStack.EMPTY;

    //FUNCTIONALITY (audit, 2026-09-24): whether realOffHandItem above has actually been confirmed by a real
    //packet on THIS connection, as opposed to still sitting at its untouched default. Without this,
    //endQRCodeShow() restoring realOffHandItem verbatim is only correct once a real WINDOW_ITEMS/SET_SLOT
    //for this slot has actually been observed - a QR view ended before that (e.g. a stale action somehow
    //still live right after a reconnect, before the post-login inventory sync got a chance to run through
    //onSend()) would otherwise overwrite whatever the player is REALLY holding with an assumed-empty
    //default, silently deleting it from their hand.
    private boolean realOffHandItemKnown = false;

    //Whether the fake QR map is ACTUALLY currently sitting in the off-hand slot right now - set by
    //startQrCodeShow(), cleared by endQRCodeShow(). Deliberately NOT the same thing as
    //"currentAction is VIEWING_QR or VERIFYING_AUTH_ACCESS": VERIFYING_AUTH_ACCESS is also entered directly
    //from GoogleAuthGUI's "Reset Token"/"View Recovery Codes" buttons (via runGatedByAuthAccess()) without
    //the QR ever having been shown at all - onSend() below must not touch the off-hand slot in that case,
    //since there's no fake map to protect there.
    private boolean qrMapShown = false;

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
                return;
            }
        }

        //FUNCTIONALITY (found in ShadowOfHeaven's PR review, 2026-09-23): passively track the off-hand
        //slot's real content, independent of whether a QR is currently showing, from whatever the backend
        //server actually sends - this is what makes restoring it in endQRCodeShow() correct. While the fake
        //QR map genuinely IS sitting there (qrMapShown), a WINDOW_ITEMS resync is NOT simply dropped - doing
        //so would desync every OTHER slot on the client too (a full resync happens on things like a server
        //switch, a respawn, or a plugin explicitly refreshing the inventory, any of which could occur while
        //the QR view is still open). Instead, only the one slot the fake map occupies is spliced back in
        //before the (now-modified) packet is re-encoded and sent through as normal. A single-slot SET_SLOT
        //update for that exact slot has no such collateral-damage concern, so it's simply held back instead.
        switch (event.getPacketType()) {
            case WINDOW_ITEMS: {
                WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
                if (packet.getWindowId() == 0) {
                    List<ItemStack> currentItems = packet.getItems();
                    if (currentItems.size() > GoogleAuthGUI.QR_MAP_SLOT) {
                        this.realOffHandItem = currentItems.get(GoogleAuthGUI.QR_MAP_SLOT);
                        this.realOffHandItemKnown = true;
                        if (this.qrMapShown) {
                            List<ItemStack> spliced = new ArrayList<>(currentItems);
                            spliced.set(GoogleAuthGUI.QR_MAP_SLOT, FAKE_QR_MAP_ITEM);
                            packet.setItems(spliced);
                            event.markForReEncode(true);
                        }
                    }
                }
                break;
            }
            case SET_SLOT: {
                WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);
                if (packet.getWindowId() == 0 && packet.getSlot() == GoogleAuthGUI.QR_MAP_SLOT) {
                    this.realOffHandItem = packet.getItem();
                    this.realOffHandItemKnown = true;
                    if (this.qrMapShown) {
                        event.setCancelled(true);
                    }
                }
                break;
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
        this.verifyAuthAccess(actionOnCorrectInput, true);
    }

    //grantsProvenAccess: see the Spigot VerifiedPacketProcessor's equivalent for the full reasoning -
    //SECURITY (2026-09-23), must be false for a "confirm" with no actual pending action behind it (see
    //processChat() below), otherwise proving a code for an account's not-yet-enabled, freely-viewable QR
    //code could permanently bypass every future proof-of-access gate on it.
    public void verifyAuthAccess(Runnable actionOnCorrectInput, boolean grantsProvenAccess) {
        this.currentAction = CurrentAction.VERIFYING_AUTH_ACCESS;
        this.authBuilder = new VelocityAuthBuilder(this.user, correct -> {
            if (correct) {
                VelocityAuthBuilder.visualsOnProvenAccess(this.authBuilder, this.user);
                if (grantsProvenAccess) this.user.getData().getLoginParams().setHasProvenAuthAccess(true);
                this.endQRCodeShow();
                if (actionOnCorrectInput != null) actionOnCorrectInput.run();
                this.user.closeInventory();
                return;
            }
            VelocityAuthBuilder.visualsOnDeniedAccess(this.authBuilder, this.user);
        }, false);
        this.user.gui = null;
        this.authBuilder.show();
    }

    private static final String showEndMessage = Messages.getWithPrefix("google-auth-show-end");

    public void endQRCodeShow() {
        switch (this.currentAction) {
            case VIEWING_QR, VERIFYING_AUTH_ACCESS -> this.user.sendMessage(showEndMessage);
        }
        if (this.qrMapShown) {
            //showQrCode() fakes the QR image into the off-hand slot via a raw SetSlot packet (Velocity
            //has no real world to render it in, unlike Spigot's teleport-based display) - nothing ever
            //told the client to clear it again once done, so it visually stuck there indefinitely.
            //Restores realOffHandItem (whatever the backend server actually last reported for that slot,
            //tracked passively by onSend() above) rather than just sending an empty item back - a player
            //who genuinely had something there (a shield, totem, etc.) would otherwise see it vanish
            //until some unrelated packet happened to correct it. The backend server's own idea of what's
            //really in that slot was never touched by any of this either way and needs no changes -
            //writePacketSilently() bypasses packetevents' own send pipeline, so this call (like
            //showQrCode()'s fake SetSlot itself) is never mistaken for a real update by onSend().
            //
            //FUNCTIONALITY (audit, 2026-09-24): only if realOffHandItemKnown - see its own docs. Without
            //this guard, a QR view ended before this connection ever saw a real inventory sync for this
            //slot (only realistically possible right after a reconnect, per UserManager#add()'s own fix
            //above) would send realOffHandItem's untouched EMPTY default, permanently wiping whatever the
            //player was actually holding instead of merely leaving the client's already-correct display
            //alone.
            if (this.realOffHandItemKnown)
                this.user.writePacketSilently(new WrapperPlayServerSetSlot(0, 0, GoogleAuthGUI.QR_MAP_SLOT, this.realOffHandItem));
            this.qrMapShown = false;
        }
        this.currentAction = CurrentAction.NONE;
        //Every normal exit from the QR-view state funnels through here - clearing it as the single choke
        //point (rather than only in processChat()'s "cancel" case) so a stale pending action can never
        //survive to fire on some later, unrelated QR confirmation.
        this.pendingQRConfirmAction = null;
    }

    public void startQrCodeShow() {
        this.currentAction = CurrentAction.VIEWING_QR;
        this.qrMapShown = true;
    }

    private enum CurrentAction {
        NONE, SETTING_PASSWORD, VIEWING_QR, VERIFYING_AUTH_ACCESS
    }
}