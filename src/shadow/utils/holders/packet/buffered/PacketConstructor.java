package shadow.utils.holders.packet.buffered;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import shadow.systems.gui.AlixGUI;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.objects.savable.data.gui.PasswordGui;

import java.util.Arrays;

public final class PacketConstructor {

    private static void rename(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public static final class AnvilGUI {

        private static final Object ALL_ITEMS, ALL_ITEMS_VERIFIED, INVALID_INDICATE_ITEMS, INVALID_INDICATE_ITEMS_VERIFIED;
        private static final Object[]
                allItemsPackets = new Object[128],
                allItemsVerifiedPackets = new Object[128],
                invalidItemsPackets = new Object[128],
                invalidItemsVerifiedPackets = new Object[128];

        static {
            ItemStack USER_INPUT = new ItemStack(Material.PAPER);
            ItemStack LEAVE_BUTTON = new ItemStack(Material.BLACK_WOOL);
            ItemStack CONFIRM_BUTTON = new ItemStack(Material.LIME_WOOL);
            ItemStack INVALID_PASSWORD = new ItemStack(Material.RED_WOOL);
            ItemStack CANCEL = AlixGUI.GO_BACK_ITEM; //new ItemStack(Material.BARRIER);

            rename(USER_INPUT, "§0");
            rename(LEAVE_BUTTON, PasswordGui.pinLeave);
            rename(CONFIRM_BUTTON, PasswordGui.pinConfirm);
            rename(INVALID_PASSWORD, PasswordGui.invalidPassword);
            //rename(CANCEL, "§cCancel");

            ALL_ITEMS = OutWindowItemsPacketConstructor.createNMSItemList(Arrays.asList(USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON));
            ALL_ITEMS_VERIFIED = OutWindowItemsPacketConstructor.createNMSItemList(Arrays.asList(USER_INPUT, CANCEL, CONFIRM_BUTTON));
            INVALID_INDICATE_ITEMS = OutWindowItemsPacketConstructor.createNMSItemList(Arrays.asList(USER_INPUT, LEAVE_BUTTON, INVALID_PASSWORD));
            INVALID_INDICATE_ITEMS_VERIFIED = OutWindowItemsPacketConstructor.createNMSItemList(Arrays.asList(USER_INPUT, CANCEL, INVALID_PASSWORD));

            for (int i = 0; i < 128; i++) {
                allItemsPackets[i] = OutWindowItemsPacketConstructor.construct0(i + 1, 3, ALL_ITEMS);
                allItemsVerifiedPackets[i] = OutWindowItemsPacketConstructor.construct0(i + 1, 3, ALL_ITEMS_VERIFIED);
                invalidItemsPackets[i] = OutWindowItemsPacketConstructor.construct0(i + 1, 3, INVALID_INDICATE_ITEMS);
                invalidItemsVerifiedPackets[i] = OutWindowItemsPacketConstructor.construct0(i + 1, 3, INVALID_INDICATE_ITEMS_VERIFIED);
            }
        }

        public static Object allItems(int id) {
            return id < 129 ? allItemsPackets[id - 1] : OutWindowItemsPacketConstructor.construct0(id, 3, ALL_ITEMS);
        }

        public static Object allItemsVerified(int id) {
            return id < 129 ? allItemsVerifiedPackets[id - 1] : OutWindowItemsPacketConstructor.construct0(id, 3, ALL_ITEMS_VERIFIED);
        }

        public static Object invalidIndicate(int id) {
            return id < 129 ? invalidItemsPackets[id - 1] : OutWindowItemsPacketConstructor.construct0(id, 3, INVALID_INDICATE_ITEMS);
        }

        public static Object invalidIndicateVerified(int id) {
            return id < 129 ? invalidItemsVerifiedPackets[id - 1] : OutWindowItemsPacketConstructor.construct0(id, 3, INVALID_INDICATE_ITEMS_VERIFIED);
        }
    }


    private PacketConstructor() {
    }
}