package alix.velocity.systems.packets.gui.menu;

import alix.common.utils.config.alix.AlixYamlConfig;
import alix.common.utils.file.AlixFileManager;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A GUI menu's configurable title/background/items, loaded from "gui-menus/&lt;name&gt;.yml" in the
 * plugin's data folder (auto-created from a bundled default the first time it's needed) and cached in
 * memory afterward. A title/name/lore value starting with "@" is resolved as a language key (see
 * MenuItemDef#resolveText) so menu text follows the plugin's selected language like everything else.
 */
public final class MenuConfig {

    private static final ConcurrentMap<String, MenuConfig> CACHE = new ConcurrentHashMap<>();

    //Keep in sync with MenuRegistry's valid "[open-menu] <id>" ids and FileUpdater's own GUI_MENU_NAMES
    private static final String[] MENU_NAMES = {"account", "passwords", "login-settings", "google-auth", "ip-autologin"};

    public static MenuConfig get(String name) {
        return CACHE.computeIfAbsent(name, MenuConfig::load);
    }

    /**
     * Eagerly loads every known menu once, at plugin startup. Without this, a menu's gui-menus/*.yml is
     * only ever parsed the first time some player happens to open it (see {@link #get}'s lazy cache), so
     * a config mistake (unknown material, missing slot - both already warned about in
     * {@link MenuItemDef}) would only surface in the console log whenever that first player finally opens
     * that particular menu, possibly long after the server owner has stopped watching the console for
     * startup issues. Called once from Main#onEnable, after FileUpdater has already ensured every
     * gui-menus/*.yml exists on disk.
     */
    public static void preloadAll() {
        for (String name : MENU_NAMES) get(name);
    }

    /**
     * v1.5.2 ("/as reload"): drops every cached, already-parsed menu definition, so the next player to
     * open ANY menu (account/passwords/login-settings/google-auth/ip-autologin) causes a fresh
     * MenuConfig#load() - i.e. a fresh read of gui-menus/*.yml from disk, picking up whatever an admin
     * just edited (title/background/item placement/actions/lore, including "@lang-key" references,
     * which resolve against Messages' own already-live map - see AlixSystemCommand's "reload"
     * subcommand). Menus already open in a player's inventory at the moment of reload are unaffected
     * (they already have their packets built) - only the NEXT open uses the fresh definition.
     */
    public static void reloadAll() {
        CACHE.clear();
    }

    private final String name;
    private final String title;
    private final ItemStack backgroundItem;
    private final List<MenuItemDef> items;

    private MenuConfig(String name, String title, ItemStack backgroundItem, List<MenuItemDef> items) {
        this.name = name;
        this.title = title;
        this.backgroundItem = backgroundItem;
        this.items = items;
    }

    private static MenuConfig load(String name) {
        AlixYamlConfig config = AlixYamlConfig.getOrCreatePluginFile("gui-menus/" + name + ".yml", AlixFileManager.FileType.CONFIG);

        String title = MenuItemDef.resolveText(config.getString("title", ""), name);
        ItemStack background = MenuItemDef.buildIcon(config, "background", ItemTypes.GRAY_STAINED_GLASS_PANE, " ");

        List<MenuItemDef> items = new ArrayList<>();
        for (String id : config.getStringList("item-order")) {
            if (id == null || id.isBlank()) continue;
            items.add(MenuItemDef.load(config, id.trim()));
        }

        return new MenuConfig(name, title, background, Collections.unmodifiableList(items));
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public ItemStack getBackgroundItem() {
        return backgroundItem;
    }

    public List<MenuItemDef> getItems() {
        return items;
    }

    /**
     * The slot(s) configured for a given "internal" item id (see MenuItemDef), or an empty array
     * if that id isn't placed anywhere in this menu's config.
     */
    public int[] getSlotsForInternal(String internalId) {
        for (MenuItemDef def : items) {
            if (def.isInternal() && def.getInternalId().equals(internalId)) return def.getSlots();
        }
        return new int[0];
    }
}
