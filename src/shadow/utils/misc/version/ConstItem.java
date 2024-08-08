package shadow.utils.misc.version;

import org.bukkit.inventory.ItemStack;

public final class ConstItem {

    private final ItemStack item;

    ConstItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItemCloned() {
        return item.clone();
    }
}