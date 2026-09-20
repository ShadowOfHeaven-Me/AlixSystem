package alix.velocity.systems.packets.gui.impl;

import alix.common.data.PersistentUserData;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.scheduler.AlixScheduler;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.systems.packets.gui.menu.MenuBuilder;
import alix.velocity.systems.packets.gui.menu.MenuConfig;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;

import java.util.Map;

public final class IpAutoLoginGUI extends AlixGUI {

    private static final String MENU_NAME = "ip-autologin";

    private final String
            messageAccept = Messages.getWithPrefix("ip-autologin-accept"),
            messageReject = Messages.getWithPrefix("ip-autologin-reject");

    private IpAutoLoginGUI(VerifiedUser user) {
        super(user, AlixInventoryType.GENERIC_3X3, MenuConfig.get(MENU_NAME).getTitle());
    }

    @Override
    protected GUIItem[] create(InventoryGui inv) {
        MenuConfig menu = MenuConfig.get(MENU_NAME);
        int size = AlixInventoryType.GENERIC_3X3.size();
        PersistentUserData data = inv.getData();

        String[] lore2 = Messages.get("gui-ip-autologin-lore-accept").split(" -nl ");
        ItemStack confirm = create(ItemTypes.GREEN_WOOL, Messages.get("gui-ip-autologin-accept"), lore2);
        GUIItem acceptGuiItem = new GUIItem(setLore(confirm, lore2), event -> {
            data.getLoginParams().setIpAutoLogin(true);
            user.sendMessage(this.messageAccept);
            this.user.closeInventory();
        });

        String[] lore3 = Messages.get("gui-ip-autologin-lore-reject").split(" -nl ");
        ItemStack reject = create(ItemTypes.RED_WOOL, Messages.get("gui-ip-autologin-reject"), lore2);
        GUIItem rejectGuiItem = new GUIItem(setLore(reject, lore3), event -> {
            data.getLoginParams().setIpAutoLogin(false);
            user.sendMessage(this.messageReject);
            this.user.closeInventory();
        });

        Map<String, GUIItem> internalItems = Map.of(
                "accept", acceptGuiItem,
                "reject", rejectGuiItem
        );

        return MenuBuilder.build(menu, size, this.user, internalItems);
    }

    public static void add(VerifiedUser user) {
        AlixScheduler.async(() -> new IpAutoLoginGUI(user).map());
    }
}
