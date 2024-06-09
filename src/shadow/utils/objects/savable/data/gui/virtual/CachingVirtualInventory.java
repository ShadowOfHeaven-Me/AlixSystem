package shadow.utils.objects.savable.data.gui.virtual;

import alix.common.utils.AlixCommonUtils;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import shadow.utils.holders.packet.constructors.AlixInventoryType;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.netty.NettyUtils;

import java.util.List;

public final class CachingVirtualInventory extends VirtualInventory {

    private boolean spoofWithCached;
    private final ByteBuf cachedConstInvItemsPacket;

    public CachingVirtualInventory(ChannelHandlerContext silentContext, List<ItemStack> contents, ByteBuf constItemsPacket, ByteBuf constInventoryOpenBuf) {
        super(silentContext, contents, constInventoryOpenBuf, AlixCommonUtils.EMPTY_CONSUMER);//we don't want to release the constant buffers, so we give an empty destroy method
        this.cachedConstInvItemsPacket = constItemsPacket;
    }

    public void setSpoofWithCached(boolean spoofedWithCached) {
        this.spoofWithCached = spoofedWithCached;
    }

    @Override
    public void spoofAllItems() {
        if (spoofWithCached) NettyUtils.writeAndFlushConst(this.silentContext, this.cachedConstInvItemsPacket);
        else super.spoofAllItems();
    }

    @Override
    public void setItem(int i, ItemStack item) {
        super.setItem(i, item);
        this.spoofWithCached = false;
    }

    public static ByteBuf constInvItemsByteBuf(List<ItemStack> contents) {
        return OutWindowItemsPacketConstructor.constructConst0(1, 1, contents);
    }

    public static ByteBuf constInvOpenByteBuf(int size, Component title) {
        return constInvOpenByteBuf(AlixInventoryType.generic9xN(size), title);
    }

    public static ByteBuf constInvOpenByteBuf(AlixInventoryType type, Component title) {
        return NettyUtils.constBuffer(new WrapperPlayServerOpenWindow(1, type.getId(), title));//window id = 1
    }
}