package ua.nanit.limbo.protocol.packets.play.inventory;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

import java.util.List;

public final class PacketPlayOutInventoryItems extends OutRetrooperPacket<WrapperPlayServerWindowItems> {

    public PacketPlayOutInventoryItems() {
        super(WrapperPlayServerWindowItems.class);
    }

    public PacketPlayOutInventoryItems(WrapperPlayServerWindowItems wrapper) {
        super(wrapper);
    }

    public PacketPlayOutInventoryItems(List<ItemStack> itemStacks) {
        super(new WrapperPlayServerWindowItems(1, 1, itemStacks, null));
    }
}