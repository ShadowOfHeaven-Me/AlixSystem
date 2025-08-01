package alix.velocity.systems.packets.gui.impl;

import alix.common.data.PersistentUserData;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.scheduler.AlixScheduler;
import alix.velocity.systems.packets.gui.AbstractAlixGUI;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;

import java.util.Arrays;
import java.util.function.Function;

import static alix.common.utils.config.ConfigProvider.config;

public final class LoginSettingsGUI extends AlixGUI {

    private static final boolean forcefullyDisableIpAutoLogin = config.getBoolean("forcefully-disable-ip-autologin");
    private static final String loginSettingsTitle = Messages.get("gui-title-login-settings");
    private static final ItemStack
            IP_AUTOLOGIN_ON = create(ItemTypes.GREEN_CONCRETE, Messages.get("gui-login-settings-ip-autologin-on")),
            IP_AUTOLOGIN_OFF = create(ItemTypes.RED_CONCRETE, Messages.get("gui-login-settings-ip-autologin-off"));
    private static final Function<PersistentUserData, ItemStack> IP_AUTOLOGIN_GET = data -> data.getLoginParams().getIpAutoLogin() ? IP_AUTOLOGIN_ON : IP_AUTOLOGIN_OFF;

    static {
        if (forcefullyDisableIpAutoLogin)
            addLore(IP_AUTOLOGIN_ON, Messages.get("ip-autologin-forcefully-disabled").split(" -nl "));
    }

    private final AbstractAlixGUI originalGui;

    private LoginSettingsGUI(VerifiedUser user, AbstractAlixGUI originalGui) {
        super(user, AlixInventoryType.GENERIC_9X1, loginSettingsTitle);
        this.originalGui = originalGui;
    }

    @Override
    protected GUIItem[] create(InventoryGui player) {
        GUIItem[] items = new GUIItem[9];
        Arrays.fill(items, BACKGROUND_ITEM);

        PersistentUserData data = user.getData();

        ItemStack i1 = IP_AUTOLOGIN_GET.apply(data);
        items[0] = new GUIItem(i1, event -> {
            data.getLoginParams().setIpAutoLogin(!data.getLoginParams().getIpAutoLogin());
            gui.setItem(0, IP_AUTOLOGIN_GET.apply(data));
        });

        items[8] = new GUIItem(GO_BACK_ITEM, event -> {
            this.originalGui.map(); //set the originalGui gui as used
        });


        return items;
    }

    public static void add(VerifiedUser user, AbstractAlixGUI originalGui) {
        AlixScheduler.async(() -> new LoginSettingsGUI(user, originalGui).map());
    }

/*    public static void remove(Player player) {
        MAP.remove(player.getUniqueId());
    }*/
}