package alix.velocity.systems.packets.gui.impl;

import alix.common.data.LoginParams;
import alix.common.data.PersistentUserData;
import alix.common.data.fingerprinting.FingerprintGateway;
import alix.common.data.settings.ServerSettingsManager;
import alix.common.data.settings.Setting;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.config.ConfigParams;
import alix.velocity.systems.packets.gui.AbstractAlixGUI;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.systems.packets.gui.menu.MenuBuilder;
import alix.velocity.systems.packets.gui.menu.MenuConfig;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class LoginSettingsGUI extends AlixGUI {

    private static final String MENU_NAME = "login-settings";

    private static final boolean forcefullyDisableIpAutoLogin = false;//config.getBoolean("forcefully-disable-ip-autologin");

    private static final ItemStack
            IP_AUTOLOGIN_ON = create(ItemTypes.GREEN_CONCRETE, Messages.get("gui-login-settings-ip-autologin-on")),
            IP_AUTOLOGIN_OFF = create(ItemTypes.RED_CONCRETE, Messages.get("gui-login-settings-ip-autologin-off")),
            EMAIL_RECOVERY_ON = create(ItemTypes.GREEN_CONCRETE, Messages.get("gui-login-settings-email-recovery-on"), Messages.get("gui-login-settings-email-recovery-on-lore").split(" -nl ")),
            EMAIL_RECOVERY_OFF = create(ItemTypes.RED_CONCRETE, Messages.get("gui-login-settings-email-recovery-off"), Messages.get("gui-login-settings-email-recovery-off-lore").split(" -nl ")),
            DEVICE_PASSKEY_ON = create(ItemTypes.GREEN_CONCRETE, Messages.get("gui-login-settings-device-passkey-on"), Messages.get("gui-login-settings-device-passkey-on-lore").split(" -nl ")),
            DEVICE_PASSKEY_OFF = create(ItemTypes.RED_CONCRETE, Messages.get("gui-login-settings-device-passkey-off"), Messages.get("gui-login-settings-device-passkey-off-lore").split(" -nl "));

    private static final Function<PersistentUserData, ItemStack> IP_AUTOLOGIN_GET = data -> data.getLoginParams().getIpAutoLogin() ? IP_AUTOLOGIN_ON : IP_AUTOLOGIN_OFF;
    private static final Function<PersistentUserData, ItemStack> EMAIL_RECOVERY_GET = data -> data.getEmail() != null ? EMAIL_RECOVERY_ON : EMAIL_RECOVERY_OFF;
    private static final Function<PersistentUserData, ItemStack> DEVICE_PASSKEY_GET = data -> data.hasFingerprint() ? DEVICE_PASSKEY_ON : DEVICE_PASSKEY_OFF;

    static {
        if (forcefullyDisableIpAutoLogin)
            addLore(IP_AUTOLOGIN_ON, Messages.get("ip-autologin-forcefully-disabled").split(" -nl "));
    }

    private final AbstractAlixGUI originalGui;

    private LoginSettingsGUI(VerifiedUser user, AbstractAlixGUI originalGui) {
        super(user, AlixInventoryType.GENERIC_9X1, MenuConfig.get(MENU_NAME).getTitle());
        this.originalGui = originalGui;
    }

    @Override
    protected GUIItem[] create(InventoryGui inv) {
        MenuConfig menu = MenuConfig.get(MENU_NAME);
        int size = AlixInventoryType.GENERIC_9X1.size();

        PersistentUserData data = user.getData();

        int[] ipAutoLoginSlots = menu.getSlotsForInternal("ip-autologin");
        GUIItem ipAutoLoginGuiItem = new GUIItem(IP_AUTOLOGIN_GET.apply(data), event -> {
            data.getLoginParams().setIpAutoLogin(!data.getLoginParams().getIpAutoLogin());
            ItemStack updated = IP_AUTOLOGIN_GET.apply(data);
            for (int slot : ipAutoLoginSlots) gui.setItem(slot, updated);
        });

        GUIItem backGuiItem = new GUIItem(GO_BACK_ITEM, event -> this.originalGui.map());

        Map<String, GUIItem> internalItems = new HashMap<>();
        internalItems.put("ip-autologin", ipAutoLoginGuiItem);
        internalItems.put("back", backGuiItem);

        if (ServerSettingsManager.is(Setting.VERIFIED_EMAIL, true)) {
            //Clicking prompts for a new email in chat (send/confirm code) rather than a direct GUI toggle -
            //there's no valid "off" value to switch to, unlike the other settings on this screen.
            internalItems.put("email-recovery", new GUIItem(EMAIL_RECOVERY_GET.apply(data), event -> {
                user.closeInventory();
                runGatedByAuthAccess(user, () -> user.sendMessage(Messages.getWithPrefix("gui-account-change-email-chat")));
            }));
        }

        if (ConfigParams.fingerprintingEnabled) {
            int[] devicePasskeySlots = menu.getSlotsForInternal("device-passkey");
            GUIItem devicePasskeyGuiItem = new GUIItem(DEVICE_PASSKEY_GET.apply(data), event ->
                    FingerprintGateway.sendFingerprintingPacks(user.getChannel(), success -> {
                        if (!success) return;
                        ItemStack updated = DEVICE_PASSKEY_GET.apply(data);
                        for (int slot : devicePasskeySlots) gui.setItem(slot, updated);
                    }));
            internalItems.put("device-passkey", devicePasskeyGuiItem);
        }

        return MenuBuilder.build(menu, size, this.user, internalItems);
    }

    public static void add(VerifiedUser user, AbstractAlixGUI originalGui) {
        AlixScheduler.async(() -> new LoginSettingsGUI(user, originalGui).map());
    }

    //The email is this account's recovery channel, so changing it unattended would let anyone silently
    //redirect recovery to themselves - see GoogleAuthGUI's own copy of this check for the full reasoning.
    private static void runGatedByAuthAccess(VerifiedUser user, Runnable action) {
        LoginParams params = user.getData().getLoginParams();

        if (params.hasProvenAuthAccess() || !params.getAuthSettings().requiresAuthApp()) {
            action.run();
            return;
        }
        user.getDuplexProcessor().verifyAuthAccess(action);
    }
}
