package shadow.utils.misc.packet.buffered;

import io.netty.buffer.ByteBuf;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import shadow.systems.gui.AlixGUI;
import shadow.utils.misc.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.misc.version.AlixMaterials;
import shadow.utils.objects.savable.data.gui.PasswordGui;

import java.util.Arrays;
import java.util.List;

public final class PacketConstructor {

    private static void rename(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public static final class AnvilGUI {

        public static final String USER_INPUT_STR = "§f";
        public static final char[] USER_INPUT_CHAR_ARRAY = USER_INPUT_STR.toCharArray();
        public static final short MAX_CACHED = 100;//the inventory id counter is always between 0-99
        //private static final List<com.github.retrooper.packetevents.protocol.item.ItemStack> ALL_ITEMS, ALL_ITEMS_VERIFIED, INVALID_INDICATE_ITEMS, INVALID_INDICATE_ITEMS_VERIFIED;
        public static final ByteBuf allItemsPacket, invalidItemsPacket;
        private static final ByteBuf[]
                //allItemsPackets = new ByteBuf[MAX_CACHED],
                allItemsVerifiedPackets = new ByteBuf[MAX_CACHED],
                //invalidItemsPackets = new ByteBuf[MAX_CACHED],
                invalidItemsVerifiedPackets = new ByteBuf[MAX_CACHED];

        static {
            ItemStack USER_INPUT = new ItemStack(Material.PAPER);
            ItemStack LEAVE_BUTTON = AlixMaterials.BLACK_WOOL.getItemCloned();
            ItemStack CONFIRM_BUTTON = AlixMaterials.LIME_WOOL.getItemCloned();
            ItemStack INVALID_PASSWORD = AlixMaterials.RED_WOOL.getItemCloned();
            ItemStack CANCEL = AlixGUI.GO_BACK_ITEM; //new ItemStack(Material.BARRIER);

            rename(USER_INPUT, USER_INPUT_STR);
            rename(LEAVE_BUTTON, PasswordGui.pinLeave);
            rename(CONFIRM_BUTTON, PasswordGui.pinConfirm);
            rename(INVALID_PASSWORD, PasswordGui.invalidPassword);
            //rename(CANCEL, "§cCancel");

            List<alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack>
                    ALL_ITEMS = OutWindowItemsPacketConstructor.createRetrooperItemList(Arrays.asList(USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON)),
                    ALL_ITEMS_VERIFIED = OutWindowItemsPacketConstructor.createRetrooperItemList(Arrays.asList(USER_INPUT, CANCEL, CONFIRM_BUTTON)),
                    INVALID_INDICATE_ITEMS = OutWindowItemsPacketConstructor.createRetrooperItemList(Arrays.asList(USER_INPUT, LEAVE_BUTTON, INVALID_PASSWORD)),
                    INVALID_INDICATE_ITEMS_VERIFIED = OutWindowItemsPacketConstructor.createRetrooperItemList(Arrays.asList(USER_INPUT, CANCEL, INVALID_PASSWORD));

            for (int i = 0; i < MAX_CACHED; i++) {
                //allItemsPackets[i] = OutWindowItemsPacketConstructor.constructConst0(i + 1, 3, ALL_ITEMS);
                allItemsVerifiedPackets[i] = OutWindowItemsPacketConstructor.constructConst0(i + 1, 3, ALL_ITEMS_VERIFIED);
                //invalidItemsPackets[i] = OutWindowItemsPacketConstructor.constructConst0(i + 1, 3, INVALID_INDICATE_ITEMS);
                invalidItemsVerifiedPackets[i] = OutWindowItemsPacketConstructor.constructConst0(i + 1, 3, INVALID_INDICATE_ITEMS_VERIFIED);
            }
            allItemsPacket = OutWindowItemsPacketConstructor.constructConst0(1, 3, ALL_ITEMS);
            invalidItemsPacket = OutWindowItemsPacketConstructor.constructConst0(1, 3, INVALID_INDICATE_ITEMS);
        }

/*        public static ByteBuf allItems(int id) {
            return allItemsPackets[id - 1];
        }*/

        public static ByteBuf allItemsVerified(int id) {
            return allItemsVerifiedPackets[id - 1];
        }

/*        public static ByteBuf invalidIndicate(int id) {
            return invalidItemsPackets[id - 1];
        }*/

        public static ByteBuf invalidIndicateVerified(int id) {
            return invalidItemsVerifiedPackets[id - 1];
        }
    }

    private PacketConstructor() {
    }
}