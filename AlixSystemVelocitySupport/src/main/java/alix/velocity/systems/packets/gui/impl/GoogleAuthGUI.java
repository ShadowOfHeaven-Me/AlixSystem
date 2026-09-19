package alix.velocity.systems.packets.gui.impl;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
import alix.common.login.auth.GoogleAuthExplanation;
import alix.common.login.auth.GoogleAuthUtils;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.packets.inventory.click.AlixClickType;
import alix.common.packets.inventory.click.ContainerClickWrapper;
import alix.common.packets.message.MessageWrapper;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.list.LoopList;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.image.ImageGenerator;
import alix.velocity.systems.packets.gui.AbstractAlixGUI;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.changes.AuthDataChanges;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.systems.packets.gui.menu.MenuBuilder;
import alix.velocity.systems.packets.gui.menu.MenuConfig;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import ua.nanit.limbo.connection.login.gui.bedrock.AbstractAuthBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Map;

public final class GoogleAuthGUI extends AlixGUI {

    private static final String MENU_NAME = "google-auth";

    private static final ItemStack showQRCodeItem, applyChangesItem, resetTokenItem, resetTokenConfirmItem, viewRecoveryCodesItem;
    private static final AuthItemType PASSWORD, AUTH, AUTH_AND_PASSWORD;

    static {
        PASSWORD = new AuthItemType(setLore(create(ItemTypes.OBSIDIAN, Messages.get("gui-google-auth-config-password-name")), Messages.getSplit("gui-google-auth-config-password-lore")), AuthSetting.PASSWORD);
        AUTH = new AuthItemType(setLore(create(ItemTypes.NETHER_STAR, Messages.get("gui-google-auth-config-auth-name")), Messages.getSplit("gui-google-auth-config-auth-lore")), AuthSetting.AUTH_APP);
        AUTH_AND_PASSWORD = new AuthItemType(setLore(create(ItemTypes.BEACON, Messages.get("gui-google-auth-config-auth-and-password-name")), Messages.getSplit("gui-google-auth-config-auth-and-password-lore")), AuthSetting.PASSWORD_AND_AUTH_APP);
    }

    private static final AuthItemType[] AUTH_TYPES = {
            PASSWORD, AUTH, AUTH_AND_PASSWORD
    };

    static {
        showQRCodeItem = AbstractAuthBuilder.ofSkull(Messages.get("gui-google-auth-show-qr-code-name"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTUzYzE0OTUwZmMzNjQ2NzhiNzU1NDRhY2IxZGEwYzk0MjBiNTA2ZTU4NzEyMDM5M2IzZDFhZDQ4OThlNzRmIn19fQ==");
        String[] loreQRCode = Messages.get("gui-google-auth-show-qr-code-lore").split(" -nl ");
        setLore(showQRCodeItem, loreQRCode);

        applyChangesItem = create(ItemTypes.GREEN_CONCRETE, Messages.get("gui-google-auth-apply-changes"));

        resetTokenItem = create(ItemTypes.RED_CONCRETE, Messages.get("gui-google-auth-reset-token-name"),
                Messages.get("gui-google-auth-reset-token-lore").split(" -nl "));

        resetTokenConfirmItem = create(ItemTypes.TNT, Messages.get("gui-google-auth-reset-token-confirm-name"),
                Messages.get("gui-google-auth-reset-token-confirm-lore").split(" -nl "));

        viewRecoveryCodesItem = create(ItemTypes.PAPER, Messages.get("gui-google-auth-view-recovery-codes-name"),
                Messages.get("gui-google-auth-view-recovery-codes-lore").split(" -nl "));
    }

    private final AbstractAlixGUI originalGui;
    //Armed by a first click on the "reset-token" item, cleared on a second (confirming) click - a fresh
    //instance is created per GUI open (see add() below), so this naturally resets on reopen.
    private boolean resetArmed;

    private GoogleAuthGUI(VerifiedUser user, AbstractAlixGUI originalGui) {
        super(user, AlixInventoryType.GENERIC_9X3, MenuConfig.get(MENU_NAME).getTitle());
        this.originalGui = originalGui;
    }

    @Override
    protected GUIItem[] create(InventoryGui inv) {
        MenuConfig menu = MenuConfig.get(MENU_NAME);
        int size = AlixInventoryType.GENERIC_9X3.size();
        AuthDataChanges changes = new AuthDataChanges();

        LoginParams params = user.getData().getLoginParams();

        LoopList<AuthItemType> authList = LoopList.of(AUTH_TYPES);
        authList.setCurrentIndex(authList.indexOfFirst(t -> t.authSetting.equals(params.getAuthSettings())));

        GUIItem backGuiItem = new GUIItem(GO_BACK_ITEM, event -> this.originalGui.map());//set the originalGui gui as used

        GUIItem showQRGuiItem = new GUIItem(showQRCodeItem, e -> this.user.getChannel().eventLoop().execute(this::showQrCode));

        int[] resetTokenSlots = menu.getSlotsForInternal("reset-token");
        GUIItem resetTokenGuiItem = new GUIItem(resetTokenItem, event -> {
            if (!this.resetArmed) {
                this.resetArmed = true;
                for (int slot : resetTokenSlots) gui.setItem(slot, resetTokenConfirmItem);
                return;
            }

            this.resetArmed = false;
            this.user.getChannel().eventLoop().execute(() -> {
                this.user.getData().regenerateAuthToken();
                String[] codes = this.user.getData().regenerateRecoveryCodes();

                this.user.user.sendMessage(Messages.getWithPrefix("gui-google-auth-reset-token-success-chat"));
                sendRecoveryCodes(this.user, codes);

                this.showQrCode();
            });
        });

        GUIItem viewRecoveryCodesGuiItem = new GUIItem(viewRecoveryCodesItem, event -> {
            this.user.closeInventory();
            this.user.getData().loadRecoveryCodes(codes ->
                    this.user.getChannel().eventLoop().execute(() -> sendRecoveryCodes(this.user, codes)));
        });

        int[] authTypeSlots = menu.getSlotsForInternal("auth-type");
        GUIItem authTypeGuiItem = new GUIItem(authList.current().item, event -> {
            var type = ContainerClickWrapper.getAlixClickType(event);
            switch (type) {
                case LEFT_CLICK:
                case RIGHT_CLICK:
                    AuthItemType c = type == AlixClickType.RIGHT_CLICK ? authList.previous() : authList.next();
                    changes.setAuthSetting(c.authSetting);
                    for (int slot : authTypeSlots) gui.setItem(slot, c.item);
                    break;
            }
        });

        GUIItem applyChangesGuiItem = new GUIItem(applyChangesItem, event -> changes.tryApply(user));

        Map<String, GUIItem> internalItems = Map.of(
                "back", backGuiItem,
                "show-qr", showQRGuiItem,
                "auth-type", authTypeGuiItem,
                "apply-changes", applyChangesGuiItem,
                "reset-token", resetTokenGuiItem,
                "view-recovery-codes", viewRecoveryCodesGuiItem
        );

        return MenuBuilder.build(menu, size, this.user, internalItems);
    }

    //Renders and shows the current Google Authenticator QR code - shared by the "show-qr" button and by a
    //successful "reset-token" confirmation (which needs to show the BRAND NEW code right after generating
    //it). Must run on this.user's event loop - callers are responsible for that (see the two call sites).
    private void showQrCode() {
        var token = this.user.getData().getToken();
        try {
            byte[] imgBytes = GoogleAuthUtils.createQRCode(
                    GoogleAuthUtils.getGoogleAuthenticatorBarCode(token, "#1", "AlixVelocity"),
                    128, 128
            );

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgBytes));
            //Main.logInfo("image w=" + image.getWidth() + " h=" + image.getHeight());

            byte[] serialized = ImageGenerator.imageToBytes(image);

            ItemStack mapItem = ItemStack.builder().type(ItemTypes.FILLED_MAP).amount(1).component(ComponentTypes.MAP_ID, 0).build();

            WrapperPlayServerSetSlot setSlotPacket = new WrapperPlayServerSetSlot(0, 0, 45, mapItem);

            WrapperPlayServerMapData mapDataPacket = new WrapperPlayServerMapData(0, (byte) 3, false, true,
                    null, image.getWidth(), image.getHeight(), 0, 0, serialized);

            this.user.getDuplexProcessor().startQrCodeShow();

            this.user.writePacketSilently(setSlotPacket);
            this.user.writePacketSilently(mapDataPacket);
            this.user.writePacketSilently(MessageWrapper.createWrapper(GoogleAuthExplanation.COMBINED, false, this.user.user.getClientVersion().toServerVersion()));
            this.user.closeInventory();//flush
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void sendRecoveryCodes(VerifiedUser user, String[] codes) {
        user.user.sendMessage(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-header"));
        for (String code : codes) {
            user.user.sendMessage(AlixFormatter.translateColors("&e" + code));
        }
        user.user.sendMessage(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-footer"));
    }

    public static void add(VerifiedUser user, AbstractAlixGUI originalGui) {
        AlixScheduler.async(() -> new GoogleAuthGUI(user, originalGui).map());
    }

    private static final class AuthItemType {

        private final ItemStack item;
        private final AuthSetting authSetting;

        private AuthItemType(ItemStack item, AuthSetting authSetting) {
            this.item = item;
            this.authSetting = authSetting;
        }
    }
}
