package alix.velocity.systems.packets.gui.menu;

import alix.common.AlixCommonMain;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.utils.user.VerifiedUser;

import java.util.Arrays;
import java.util.Map;

/**
 * Shared logic for turning a MenuConfig into a background-filled GUIItem[] array, used by every
 * menu. Regular (non-"internal") items are built and wired to their configured actions here;
 * "internal" items are looked up by id in the internalItems map the calling menu supplies (its
 * own live, stateful buttons - see MenuItemDef).
 */
public final class MenuBuilder {

    public static GUIItem[] build(MenuConfig menu, int size, VerifiedUser user, Map<String, GUIItem> internalItems) {
        GUIItem[] items = new GUIItem[size];
        Arrays.fill(items, new GUIItem(menu.getBackgroundItem()));

        for (MenuItemDef def : menu.getItems()) {
            GUIItem guiItem;
            if (def.isInternal()) {
                //a missing entry here isn't necessarily a config mistake - some internal items only exist
                //when a related setting is enabled (e.g. email recovery), so this is a silent no-op rather
                //than a logged warning; the item's slot simply stays background.
                guiItem = internalItems.get(def.getInternalId());
                if (guiItem == null) continue;
            } else {
                guiItem = new GUIItem(def.getIcon(), event -> def.getActions().forEach(action -> action.run(user)));
            }

            for (int slot : def.getSlots()) {
                if (slot < 0 || slot >= size) {
                    AlixCommonMain.logWarning("gui-menus/" + menu.getName() + ".yml: item '" + def.getId()
                            + "' has an out-of-range slot " + slot + " for a " + size + "-slot menu, skipping it");
                    continue;
                }
                items[slot] = guiItem;
            }
        }

        return items;
    }

    private MenuBuilder() {
    }
}
