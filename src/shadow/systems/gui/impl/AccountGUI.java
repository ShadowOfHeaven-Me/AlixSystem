package shadow.systems.gui.impl;

import alix.common.messages.Messages;
import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import shadow.systems.gui.AlixGUI;
import shadow.systems.gui.item.GUIItem;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.version.AlixMaterials;

import java.util.Arrays;

public abstract class AccountGUI extends AlixGUI {

    /*private static final AccountGUI
            INSTANCE_MODERN = new AccountGUIModern(),
            INSTANCE_OLDER = new AccountGUIOlder();*/

    private static final AccountGUI INSTANCE =
            PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_14) ? new AccountGUIModern() : new AccountGUIOlder();

    private AccountGUI() {
        super(Bukkit.createInventory(null, InventoryType.DROPPER, Messages.get("gui-title-account")));
    }

    private static final class AccountGUIModern extends AccountGUI {

        @Override
        protected GUIItem[] create(Player unusedNull) {
            GUIItem[] items = new GUIItem[9];
            Arrays.fill(items, BACKGROUND_ITEM);

            ItemStack i1 = create(AlixMaterials.IRON_BARS.getItemCloned(), Messages.get("gui-account-passwords"));
            items[0] = new GUIItem(i1, event -> PasswordsGUI.add((Player) event.getWhoClicked(), this));

            ItemStack i2 = create(Material.NETHER_STAR, Messages.get("gui-account-login-settings"));
            items[1] = new GUIItem(i2, event -> LoginSettingsGUI.add((Player) event.getWhoClicked(), this));

            ItemStack i3 = AlixUtils.getSkull(Messages.get("gui-account-google-authenticator"),"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWE2ZDYxZjQ1M2MwMTU5ZWU3Y2Q4MWE0YzNlMDUzZTlkYTdkYzE0ODYzMTg4OTBhZDRhZDlhY2Y2MTE5NmI4NSJ9fX0=");
            items[2] = new GUIItem(i3, event -> GoogleAuthGUI.add((Player) event.getWhoClicked()));

            return items;
        }
    }

    private static final class AccountGUIOlder extends AccountGUI {

        private static final String notAvailable = Messages.getWithPrefix("gui-account-passwords-gui-not-available");

        @Override
        protected GUIItem[] create(Player unusedNull) {
            GUIItem[] items = new GUIItem[9];
            Arrays.fill(items, BACKGROUND_ITEM);

            ItemStack i1 = create(AlixMaterials.IRON_BARS.getItemCloned(), Messages.get("gui-account-passwords"), "§7(1.14+)");
            items[0] = new GUIItem(i1, event -> event.getWhoClicked().sendMessage(notAvailable));

            ItemStack i2 = create(Material.NETHER_STAR, Messages.get("gui-account-login-settings"));
            items[1] = new GUIItem(i2, event -> LoginSettingsGUI.add((Player) event.getWhoClicked(), this));

            //ItemStack i3 = AlixUtils.getSkull(Messages.get("gui-account-google-authenticator"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWE2ZDYxZjQ1M2MwMTU5ZWU3Y2Q4MWE0YzNlMDUzZTlkYTdkYzE0ODYzMTg4OTBhZDRhZDlhY2Y2MTE5NmI4NSJ9fX0=");
            //items[2] = new GUIItem(i3, event -> GoogleAuthGUI.add((Player) event.getWhoClicked()));

            return items;
        }
    }

    public static void add(Player player) {
        //User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        //user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)
        AccountGUI gui = INSTANCE; //PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_14) ? INSTANCE_MODERN : INSTANCE_OLDER;
        MAP.put(player.getUniqueId(), gui);
        player.openInventory(gui.gui);
    }

/*    public static void remove(Player player) {
        MAP.remove(player.getUniqueId());
    }*/
}