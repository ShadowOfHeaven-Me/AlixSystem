package alix.velocity.systems.packets.gui;

import alix.common.utils.AlixCommonUtils;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

import java.util.function.Consumer;

public final class GUIItem {

    private final ItemStack item;
    private final Consumer<WrapperPlayClientClickWindow> consumer;

    public GUIItem(ItemStack item) {
        this(item, AlixCommonUtils.EMPTY_CONSUMER);
    }

    public GUIItem(ItemStack item, Consumer<WrapperPlayClientClickWindow> consumer) {
        this.item = item;
        this.consumer = consumer;
    }

    public ItemStack getItem() {
        return item;
    }

    public Consumer<WrapperPlayClientClickWindow> getConsumer() {
        return consumer;
    }
}