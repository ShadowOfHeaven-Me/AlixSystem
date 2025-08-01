package alix.velocity.systems.packets.gui;

import alix.common.packets.inventory.AlixInventoryType;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.other.throwable.AlixException;
import alix.velocity.systems.packets.gui.inv.AbstractInventory;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEnchantments;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.connection.login.gui.LimboAnvilBuilder;
import ua.nanit.limbo.connection.login.gui.LimboAuthBuilder;

import java.util.ArrayList;
import java.util.Map;

import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.getItemLore;

public abstract class AlixGUI implements AbstractAlixGUI {

    protected static final GUIItem BACKGROUND_ITEM = new GUIItem(LimboAuthBuilder.BACKGROUND_ITEM);
    public static final ItemStack GO_BACK_ITEM = LimboAnvilBuilder.GO_BACK_ITEM;
    private final GUIItem[] items;
    public final VerifiedUser user;
    protected final InventoryGui gui;

    public AlixGUI(VerifiedUser user, AlixInventoryType type, String title) {
        this(new InventoryGui(user, type, title));
    }

    protected AlixGUI(InventoryGui gui) {
        this.gui = gui;
        this.user = gui.getUser();
        this.items = this.create(gui);
        if (items.length != gui.getSize())
            throw new AlixException("In class " + getClass().getSimpleName() + " item size is " + items.length + " for the inventory size being " + gui.getSize() + ". Therefore a mismatch appeared!");

        for (int i = 0; i < items.length; i++)
            gui.setItem(i, items[i].getItem());
    }

    protected abstract GUIItem[] create(InventoryGui gui);

    protected void set(int i, GUIItem item) {
        this.items[i] = item;
        this.gui.setItem(i, item.getItem());
    }

    @Override
    public final AbstractInventory getGUI() {
        return gui;
    }

    @Override
    public final void onClick(WrapperPlayClientClickWindow packet) {
        int slot = packet.getSlot();
        GUIItem item = slot >= 0 && slot < this.items.length ? this.items[slot] : null;
        if (item != null) {
            item.getConsumer().accept(packet);
            if (this == user.gui) this.gui.spoofItems();
        }
    }

    protected static ItemStack.Builder builderCopy(ItemStack i) {
        var copy = i.copy();
        return ItemStack.builder().type(i.getType()).components(copy.getComponents()).nbt(copy.getNBT()).legacyData(i.getLegacyData()).amount(i.getAmount());
    }

    protected static ItemStack rename(ItemStack i, String name) {
        return builderCopy(i)
                .component(ComponentTypes.CUSTOM_NAME, Component.text(AlixFormatter.translateColors(name))).build();
    }

    protected static ItemStack enchant(ItemStack i) {
        return builderCopy(i)
                .component(ComponentTypes.ENCHANTMENTS, new ItemEnchantments(Map.of(EnchantmentTypes.UNBREAKING, 1), false)).build();
    }

    protected static void addLore(ItemStack item, String... lore) {
        ItemLore list = item.getComponent(ComponentTypes.LORE).orElse(new ItemLore(new ArrayList<>()));
        list.getLines().addAll(getItemLore(lore).getLines());
    }

    protected static ItemStack setLore(ItemStack item, String... lore) {
        return builderCopy(item).component(ComponentTypes.LORE, getItemLore(lore)).build();
    }

    protected static ItemStack create(ItemType type, String displayName, String... lore) {
        return LimboAuthBuilder.of(type, displayName, lore);
    }
}