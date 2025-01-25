package shadow.utils.misc.packet.buffered;

import alix.common.data.security.password.Password;
import io.netty.buffer.ByteBuf;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import shadow.systems.gui.AlixGUI;
import shadow.utils.misc.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.misc.version.AlixMaterials;
import shadow.utils.objects.savable.data.gui.PasswordGui;

import java.util.Arrays;

public final class PacketConstructor {

    private static void rename(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public static final class AnvilGUI {

        private static final String USER_INPUT_STR = "§f";
        private static final short MAX_CACHED = 100;//the inventory id counter is always between 0-99
        private static final int MAX_PASSWORD_LEN = Password.MAX_PASSWORD_LEN;
        private static final ByteBuf[]
                allItemsInvis = new ByteBuf[MAX_CACHED],
                allItems = new ByteBuf[MAX_CACHED],
                invalidItemsInvis = new ByteBuf[MAX_CACHED],
                invalidItems = new ByteBuf[MAX_CACHED];

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

            for (int i = 0; i <= MAX_PASSWORD_LEN; i++) {
                ItemStack invisUserInput = new ItemStack(Material.PAPER);
                String name = repeat('*', i);
                rename(invisUserInput, name.isEmpty() ? USER_INPUT_STR : name);

                allItemsInvis[i] = fromItems(1, invisUserInput, LEAVE_BUTTON, CONFIRM_BUTTON);
                invalidItemsInvis[i] = fromItems(1, invisUserInput, LEAVE_BUTTON, INVALID_PASSWORD);
            }
            for (int i = 0; i < MAX_CACHED; i++) {
                allItems[i] = fromItems(i + 1, USER_INPUT, CANCEL, CONFIRM_BUTTON);
                invalidItems[i] = fromItems(i + 1, USER_INPUT, CANCEL, INVALID_PASSWORD);
            }
        }

        private static ByteBuf fromItems(int id, ItemStack... items) {
            return OutWindowItemsPacketConstructor.constructConst0(id, 3, OutWindowItemsPacketConstructor.createRetrooperItemList(Arrays.asList(items)));
        }

        private static String repeat(char c, int len) {
            char[] a = new char[len];
            Arrays.fill(a, c);
            return new String(a);
        }

        static ByteBuf allItems(int id) {
            return allItems[id - 1];
        }

        static ByteBuf invalidIndicate(int id) {
            return invalidItems[id - 1];
        }

        static ByteBuf allItemsInvis(int len) {
            return allItemsInvis[Math.min(len, MAX_PASSWORD_LEN)];
        }

        static ByteBuf invalidIndicateInvis(int len) {
            return invalidItemsInvis[Math.min(len, MAX_PASSWORD_LEN)];
        }
    }

    private PacketConstructor() {
    }
}