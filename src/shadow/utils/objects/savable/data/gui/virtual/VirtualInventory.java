package shadow.utils.objects.savable.data.gui.virtual;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import shadow.utils.misc.packet.constructors.AlixInventoryType;
import shadow.utils.misc.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.netty.NettyUtils;

import java.util.List;
import java.util.function.Consumer;

public class VirtualInventory {

    public static final int CONST_WINDOW_ID = 1;
    final List<ItemStack> list;
    final ChannelHandlerContext silentContext;
    //private final int windowId, stateId;
    private final ByteBuf openInvBuffer;
    private final Consumer<ByteBuf> destroyInvOpen;

    VirtualInventory(ChannelHandlerContext silentContext, List<ItemStack> contents, ByteBuf openInvByteBuf, Consumer<ByteBuf> destroyInvOpen) {
        this.list = contents;
        this.silentContext = silentContext;
        //this.windowId = this.stateId = 1;
        this.openInvBuffer = openInvByteBuf;
        this.destroyInvOpen = destroyInvOpen;
    }

    public VirtualInventory(ChannelHandlerContext silentContext, AlixInventoryType type, Component title, List<ItemStack> contents) {
        this(
                silentContext,
                contents,
                CachingVirtualInventory.constInvOpenByteBuf(type, title),
                b -> b.unwrap().release());//it's unreleasable unless we unwrap it (defined by NettyUtils.constBuffer)
    }

    public VirtualInventory(ChannelHandlerContext silentContext, int size, Component title, List<ItemStack> contents) {
        this(silentContext, AlixInventoryType.generic9xN(size), title, contents);
    }

    public void destroy() {
        this.destroyInvOpen.accept(this.openInvBuffer);
    }

    void openWindow() {
        NettyUtils.writeAndFlushConst(this.silentContext, this.openInvBuffer);
    }

    public void open() {
        this.openWindow();
        this.spoofAllItems();
    }

    public void setItem(int i, ItemStack item) {
        this.list.set(i, item);
        //NettyUtils.writeDynamicWrapper(new WrapperPlayServerSetSlot(this.windowId, this.stateId, i, item), this.silentContext);
    }

    public void spoofAllItems() {
        if (this.list != null)
            NettyUtils.writeDynamicWrapper(OutWindowItemsPacketConstructor.constructDynamic0(CONST_WINDOW_ID, CONST_WINDOW_ID, this.list), this.silentContext);//window and state id can be the same
    }
}