package shadow.systems.gui.item;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

import static alix.common.utils.AlixCommonUtils.EMPTY_CONSUMER;

public final class GUIItem {

    private final ItemStack item;
    private final Consumer<InventoryClickEvent> consumer;

    public GUIItem(ItemStack item) {
        this(item, EMPTY_CONSUMER);
    }

    public GUIItem(ItemStack item, Consumer<InventoryClickEvent> consumer) {
        this.item = item;
        this.consumer = consumer;
    }

    public ItemStack getItem() {
        return item;
    }

    public Consumer<InventoryClickEvent> getConsumer() {
        return consumer;
    }
}