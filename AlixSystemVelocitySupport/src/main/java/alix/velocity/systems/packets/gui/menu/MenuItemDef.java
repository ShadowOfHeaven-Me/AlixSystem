package alix.velocity.systems.packets.gui.menu;

import alix.common.AlixCommonMain;
import alix.common.messages.Messages;
import alix.common.utils.config.alix.AlixYamlConfig;
import alix.common.utils.formatter.AlixFormatter;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemModel;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import ua.nanit.limbo.connection.login.gui.bedrock.AbstractAuthBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * One configured menu item: which slot(s) it occupies, and either (a) a fully config-owned
 * appearance + click action list, or (b) an "internal" id, for the handful of buttons per menu
 * whose appearance/behavior is inherently dynamic (a live setting toggle, a cycling value, an
 * input flow) and stays implemented in Java - config only controls where that button is placed.
 */
public final class MenuItemDef {

    private final String id;
    private final int[] slots;
    private final String internalId;
    private final ItemStack icon;
    private final List<MenuAction> actions;

    private MenuItemDef(String id, int[] slots, String internalId, ItemStack icon, List<MenuAction> actions) {
        this.id = id;
        this.slots = slots;
        this.internalId = internalId;
        this.icon = icon;
        this.actions = actions;
    }

    public String getId() {
        return id;
    }

    public int[] getSlots() {
        return slots;
    }

    public boolean isInternal() {
        return internalId != null;
    }

    public String getInternalId() {
        return internalId;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<MenuAction> getActions() {
        return actions;
    }

    static MenuItemDef load(AlixYamlConfig config, String id) {
        String base = "items." + id;
        String prefix = base + ".";

        //"slot" is the only key the default configs ever actually set; "slots" (plural) is tolerated as an
        //alias but must only be looked up (and only via the quiet getter) when "slot" itself is genuinely
        //absent - looking it up unconditionally as a default-value expression, as before, evaluated it (and
        //logged a spurious "not found" warning for it) on every single item, even when "slot" was present.
        String slotRaw = config.getStringQuiet(prefix + "slot", "");
        if (slotRaw.isBlank()) slotRaw = config.getStringQuiet(prefix + "slots", "");
        int[] slots = parseSlots(slotRaw, base);

        //"internal" is only set by the handful of stateful buttons per menu (a live setting toggle, a
        //cycling value, an input flow) - most items don't set it, so this must not warn when absent.
        String internalId = config.getStringQuiet(prefix + "internal", "");
        if (!internalId.isBlank()) return new MenuItemDef(id, slots, internalId.trim(), null, List.of());

        ItemStack icon = buildIcon(config, base, ItemTypes.STONE, id);

        List<MenuAction> actions = new ArrayList<>();
        for (String raw : config.getStringList(prefix + "actions")) {
            MenuAction action = MenuAction.parse(raw);
            if (action != null) actions.add(action);
        }

        return new MenuItemDef(id, slots, null, icon, actions);
    }

    private static int[] parseSlots(String raw, String itemDescription) {
        if (raw == null || raw.isBlank()) {
            AlixCommonMain.logWarning("Menu item '" + itemDescription + "' has no 'slot' set, it will not be shown");
            return new int[0];
        }
        List<Integer> result = new ArrayList<>();
        for (String part : raw.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            if (part.contains("-")) {
                String[] range = part.split("-", 2);
                try {
                    int from = Integer.parseInt(range[0].trim());
                    int to = Integer.parseInt(range[1].trim());
                    for (int i = Math.min(from, to); i <= Math.max(from, to); i++) result.add(i);
                } catch (NumberFormatException e) {
                    AlixCommonMain.logWarning("Invalid slot range '" + part + "' for menu item '" + itemDescription + "'");
                }
            } else {
                try {
                    result.add(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                    AlixCommonMain.logWarning("Invalid slot '" + part + "' for menu item '" + itemDescription + "'");
                }
            }
        }
        int[] arr = new int[result.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = result.get(i);
        return arr;
    }

    /**
     * Builds an ItemStack from the "&lt;base&gt;.material"/"&lt;base&gt;.head-texture"/"&lt;base&gt;.name"/
     * "&lt;base&gt;.lore"/"&lt;base&gt;.item-model"/"&lt;base&gt;.custom-model-data" keys. Used both for
     * regular menu items and for the "background" filler item.
     */
    static ItemStack buildIcon(AlixYamlConfig config, String base, ItemType defaultType, String defaultName) {
        String prefix = base + ".";

        String name = resolveText(config.getString(prefix + "name", ""), defaultName);
        String[] lore = resolveLore(config, prefix);

        //head-texture/item-model/custom-model-data are all optional per-item overrides that most items
        //never set (they use a plain "material" instead) - looked up quietly so their absence, the normal
        //case, doesn't spam a "not found" warning on every menu load.
        String headTexture = config.getStringQuiet(prefix + "head-texture", "");
        ItemStack builtBase = headTexture.isBlank()
                ? AbstractAuthBuilder.of(resolveType(config.getString(prefix + "material", ""), defaultType, base), name)
                : AbstractAuthBuilder.ofSkull(name, headTexture);

        ItemStack.Builder builder = copyBuilder(builtBase);
        builder.component(ComponentTypes.LORE, AbstractAuthBuilder.getItemLore(lore));

        String itemModel = config.getStringQuiet(prefix + "item-model", "");
        if (!itemModel.isBlank()) {
            try {
                builder.component(ComponentTypes.ITEM_MODEL, new ItemModel(new ResourceLocation(itemModel)));
            } catch (Exception e) {
                AlixCommonMain.logWarning("Invalid item-model '" + itemModel + "' for menu item '" + base + "': " + e.getMessage());
            }
        }

        int customModelData = config.getIntQuiet(prefix + "custom-model-data", -1);
        if (customModelData >= 0) builder.component(ComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        return builder.build();
    }

    /**
     * A value starting with "@" is resolved as a language key (via Messages.get, which already
     * follows the selected "language" config option and applies color-code translation); anything
     * else is treated as literal text (still color-code translated). Package-visible so MenuConfig
     * can resolve a menu's "title" the same way item names/lore are resolved.
     */
    static String resolveText(String raw, String fallbackLiteral) {
        if (raw == null || raw.isBlank()) return AlixFormatter.translateColors(fallbackLiteral);
        String trimmed = raw.trim();
        if (trimmed.startsWith("@")) return Messages.get(trimmed.substring(1).trim());
        return AlixFormatter.translateColors(trimmed);
    }

    /**
     * Lore is a list of lines. If the list has exactly one entry and it starts with "@", it's
     * resolved as a single language key holding multiple lines (the existing " -nl "-joined
     * convention used throughout this plugin's lang files, via Messages.getSplit). Otherwise each
     * list entry is resolved individually (literal text, or its own "@key" reference), one per line.
     */
    private static String[] resolveLore(AlixYamlConfig config, String prefix) {
        //Most items don't set lore at all - looked up quietly so that, unlike a genuinely malformed
        //config, "no lore configured" (the common case) doesn't log a "not found" warning.
        List<String> raw = config.getStringListQuiet(prefix + "lore");
        if (raw.size() == 1 && raw.get(0).trim().startsWith("@")) {
            return Messages.getSplit(raw.get(0).trim().substring(1).trim());
        }
        String[] lore = new String[raw.size()];
        for (int i = 0; i < lore.length; i++) lore[i] = resolveText(raw.get(i), "");
        return lore;
    }

    private static ItemType resolveType(String materialName, ItemType defaultType, String itemDescription) {
        if (materialName == null || materialName.isBlank()) return defaultType;
        String trimmed = materialName.trim();

        ItemType type = ItemTypes.getByName(trimmed.toUpperCase());
        if (type == null) type = ItemTypes.getByName(trimmed.toLowerCase());
        if (type == null) type = ItemTypes.getByName("minecraft:" + trimmed.toLowerCase());

        if (type == null) {
            AlixCommonMain.logWarning("Unknown material '" + materialName + "' for menu item '" + itemDescription + "', using a fallback item instead");
            return defaultType;
        }
        return type;
    }

    //Mirrors alix.velocity.systems.packets.gui.AlixGUI#builderCopy - duplicated here since that helper is
    //package/subclass-protected and this class isn't an AlixGUI subclass. Same ItemStack API either way.
    private static ItemStack.Builder copyBuilder(ItemStack i) {
        ItemStack copy = i.copy();
        return ItemStack.builder().type(i.getType()).components(copy.getComponents()).nbt(copy.getNBT()).legacyData(i.getLegacyData()).amount(i.getAmount());
    }
}
