package shadow.systems.gui.impl;

import alix.common.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import shadow.systems.gui.AlixGUI;
import shadow.systems.gui.item.GUIItem;

import java.util.Arrays;

public final class AccountGUI extends AlixGUI {

    private static final AccountGUI INSTANCE = new AccountGUI();

    private AccountGUI() {
        super(Bukkit.createInventory(null, InventoryType.DROPPER, Messages.get("gui-title-account")));
    }

    @Override
    protected GUIItem[] create(Player unusedNull) {
        GUIItem[] items = new GUIItem[9];
        Arrays.fill(items, BACKGROUND_ITEM);

        ItemStack i1 = create(Material.IRON_BARS, Messages.get("gui-account-passwords"));
        items[0] = new GUIItem(i1, event -> PasswordsGUI.add((Player) event.getWhoClicked(), this));

        ItemStack i2 = create(Material.NETHER_STAR, Messages.get("gui-account-login-settings"));
        items[1] = new GUIItem(i2, event -> LoginSettingsGUI.add((Player) event.getWhoClicked(), this));

        return items;
    }

    public static void add(Player player) {
        MAP.put(player.getUniqueId(), INSTANCE);
        player.openInventory(INSTANCE.gui);
    }

/*    public static void remove(Player player) {
        MAP.remove(player.getUniqueId());
    }*/
}