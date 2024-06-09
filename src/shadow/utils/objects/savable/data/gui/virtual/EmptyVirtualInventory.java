package shadow.utils.objects.savable.data.gui.virtual;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import shadow.utils.holders.packet.constructors.AlixInventoryType;

public final class EmptyVirtualInventory extends VirtualInventory {

    public EmptyVirtualInventory(ChannelHandlerContext silentContext, AlixInventoryType type, Component title) {
        super(silentContext, type, title, null);
    }

    @Override
    public void setItem(int i, ItemStack item) {
        //throw new UnsupportedOperationException();
    }

    @Override
    public void spoofAllItems() {
        //throw new UnsupportedOperationException();
    }
}