package shadow.utils.objects.savable.data.gui.virtual;

import alix.common.utils.AlixCommonUtils;
import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.manager.server.ServerVersion;
import alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import shadow.utils.misc.packet.constructors.AlixInventoryType;
import shadow.utils.misc.packet.constructors.OutWindowItemsPacketConstructor;
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
        if (spoofWithCached) this.spoofCachedItems();
        else super.spoofAllItems();
    }

    public void spoofCachedItems() {
        NettyUtils.writeAndFlushConst(this.silentContext, this.cachedConstInvItemsPacket);
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

    private static final ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();

    //https://c4k3.github.io/wiki.vg/Protocol.html#Open_Window
    //https://c4k3.github.io/wiki.vg/Inventory.html
    public static ByteBuf constInvOpenByteBuf(AlixInventoryType type, Component title) {
        return NettyUtils.constBuffer(invOpenWrapper(type, title));
    }

    public static ByteBuf invOpenByteBuf(AlixInventoryType type, Component title) {
        return NettyUtils.createBuffer(invOpenWrapper(type, title));
    }

    public static WrapperPlayServerOpenWindow invOpenWrapper(AlixInventoryType type, Component title) {
        WrapperPlayServerOpenWindow wrapper;

        //window id = 1
        if (version.isNewerThanOrEquals(ServerVersion.V_1_14))
            wrapper = new WrapperPlayServerOpenWindow(VirtualInventory.CONST_WINDOW_ID, type.getId(), title);
        else {
            String oldType = type == AlixInventoryType.ANVIL ? "minecraft:anvil" : "minecraft:container";
            wrapper = new WrapperPlayServerOpenWindow(VirtualInventory.CONST_WINDOW_ID, oldType, title, type.size(), -1);
        }

        return wrapper;
    }
}