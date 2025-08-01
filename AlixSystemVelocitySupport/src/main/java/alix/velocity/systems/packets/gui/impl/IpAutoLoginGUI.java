package alix.velocity.systems.packets.gui.impl;

import alix.common.data.PersistentUserData;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.scheduler.AlixScheduler;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ua.nanit.limbo.connection.login.gui.LimboAuthBuilder;

import java.util.Arrays;

public final class IpAutoLoginGUI extends AlixGUI {

    private final String
            messageAccept = Messages.getWithPrefix("ip-autologin-accept"),
            messageReject = Messages.getWithPrefix("ip-autologin-reject");

    private IpAutoLoginGUI(VerifiedUser user) {
        super(user, AlixInventoryType.GENERIC_3X3, Messages.get("gui-title-ip-autologin"));
    }

    @Override
    protected GUIItem[] create(InventoryGui gui) {
        GUIItem[] items = new GUIItem[9];
        Arrays.fill(items, BACKGROUND_ITEM);
        PersistentUserData data = gui.getData();

        ItemStack questionMark = LimboAuthBuilder.ofSkull(Messages.get("gui-ip-autologin-question-mark"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");
        String[] lore = Messages.get("gui-ip-autologin-what-is-this").split(" -nl ");
        items[1] = new GUIItem(setLore(questionMark, lore));

        String[] lore2 = Messages.get("gui-ip-autologin-lore-accept").split(" -nl ");
        ItemStack confirm = create(ItemTypes.GREEN_WOOL, Messages.get("gui-ip-autologin-accept"), lore2);
        items[6] = new GUIItem(setLore(confirm, lore2), event -> {
            data.getLoginParams().setIpAutoLogin(true);
            user.sendMessage(this.messageAccept);
            this.user.closeInventory();
        });

        String[] lore3 = Messages.get("gui-ip-autologin-lore-reject").split(" -nl ");
        ItemStack reject = create(ItemTypes.RED_WOOL, Messages.get("gui-ip-autologin-reject"), lore2);
        items[8] = new GUIItem(setLore(reject, lore3), event -> {
            data.getLoginParams().setIpAutoLogin(false);
            user.sendMessage(this.messageReject);
            this.user.closeInventory();
        });

        return items;
    }

    public static void add(VerifiedUser user) {
        AlixScheduler.async(() -> new IpAutoLoginGUI(user).map());
    }

    public static void init() {
    }
}
