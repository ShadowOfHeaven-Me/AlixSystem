package shadow.systems.gui;

import alix.common.messages.Messages;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.other.throwable.AlixException;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import shadow.systems.gui.item.GUIItem;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.savable.data.gui.PasswordGui;

import java.util.*;

public abstract class AlixGUI implements AbstractAlixGUI {

    public static final Map<UUID, AbstractAlixGUI> MAP = new HashMap<>();
    protected static final GUIItem BACKGROUND_ITEM = new GUIItem(PasswordGui.BACKGROUND_ITEM);
    public static final ItemStack GO_BACK_ITEM = AlixUtils.getSkull(Messages.get("gui-overall-go-back"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOThiNWU5ZDVhZmFjMTgzZjFmNTcwYzFiNmVmNTE1NmMxMjFjMWVmYmQ4NTUyN2Q4ZDc5ZDBhZGVlYjY3MjQ4NSJ9fX0=");
    protected final GUIItem[] items;
    protected final Inventory gui;

    protected AlixGUI(Inventory gui) {
        this(gui, null);
    }

    protected AlixGUI(Inventory gui, Player player) {
        this.gui = gui;
        this.items = this.create(player);
        if (items.length != gui.getSize())
            throw new AlixException("In class " + getClass().getSimpleName() + " item size is " + items.length + " for the inventory size being " + gui.getSize() + ". Therefore a mismatch appeared!");
        for (int i = 0; i < items.length; i++)
            gui.setItem(i, items[i].getItem());
    }

    protected abstract GUIItem[] create(Player player);

    @Override
    public final Inventory getGUI() {
        return gui;
    }

    @Override
    public final void onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        GUIItem item = slot >= 0 && slot < this.items.length ? this.items[slot] : null;
        if (item != null) item.getConsumer().accept(event);
    }

    protected static ItemStack rename(ItemStack i, String s) {
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(AlixFormatter.translateColors(s));
        i.setItemMeta(meta);
        return i;
    }

    private static final Enchantment RANDOM_JUST_FOR_GLOW = Enchantment.values()[0];//because of version name changes

    protected static ItemStack enchant(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(RANDOM_JUST_FOR_GLOW, 10, true);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    protected static ItemStack addLore(ItemStack item, String... lore) {
        ItemMeta meta = item.getItemMeta();
        List<String> newLore = new ArrayList<>();
        List<String> existingLore = meta.getLore();
        if (existingLore != null) newLore.addAll(existingLore);
        newLore.addAll(AlixUtils.getItemLore(lore));
        meta.setLore(newLore);
        item.setItemMeta(meta);
        return item;
    }

    protected static ItemStack setLore(ItemStack item, String... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(AlixUtils.getItemLore(lore));
        item.setItemMeta(meta);
        return item;
    }

    protected static ItemStack create(ItemStack item, String displayName, String... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(AlixFormatter.translateColors(displayName));
        meta.setLore(AlixUtils.getItemLore(lore));
        item.setItemMeta(meta);
        return item;
    }
    
    protected static ItemStack create(Material type, String displayName, String... lore) {
        return create(new ItemStack(type), displayName, lore);
    }
}