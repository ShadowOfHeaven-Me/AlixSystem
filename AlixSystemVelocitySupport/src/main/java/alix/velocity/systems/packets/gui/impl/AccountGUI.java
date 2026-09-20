package alix.velocity.systems.packets.gui.impl;

import alix.common.packets.inventory.AlixInventoryType;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.systems.packets.gui.menu.MenuBuilder;
import alix.velocity.systems.packets.gui.menu.MenuConfig;
import alix.velocity.utils.user.VerifiedUser;

import java.util.Map;

/**
 * The /account menu. Its title, item positions/appearance, and click actions are configurable via
 * "gui-menus/account.yml" (auto-created with defaults on first use). See the "menu" sibling package
 * for the shared config-driven GUI engine reused by this and the other account menus.
 */
public final class AccountGUI extends AlixGUI {

    private static final String MENU_NAME = "account";

    private AccountGUI(InventoryGui inv) {
        super(inv);
    }

    public static void add(VerifiedUser user) {
        MenuConfig menu = MenuConfig.get(MENU_NAME);
        new AccountGUI(new InventoryGui(user, AlixInventoryType.GENERIC_3X3, menu.getTitle())).map();
    }

    @Override
    protected GUIItem[] create(InventoryGui inv) {
        MenuConfig menu = MenuConfig.get(MENU_NAME);
        int size = AlixInventoryType.GENERIC_3X3.size();
        return MenuBuilder.build(menu, size, this.user, Map.of());
    }
}
