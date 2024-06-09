package shadow.utils.objects.savable.data.gui.bases;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.netty.NettyUtils;

import java.util.function.IntFunction;

public abstract class AnvilBuilderBase {

    private final ChannelHandlerContext ctx;
    private final IntFunction<ByteBuf> allItemsSupplier, invalidIndicateItemsSupplier;
    private ByteBuf allItemsBuffer, invalidIndicateItemsBuffer;
    protected String password = "", invalidityReason;
    protected boolean isPasswordValid;

    protected AnvilBuilderBase(ChannelHandlerContext ctx, boolean isPasswordValid, IntFunction<ByteBuf> allItemsSupplier, IntFunction<ByteBuf> invalidIndicateItemsSupplier) {
        this.ctx = ctx;
        this.isPasswordValid = isPasswordValid;
        this.allItemsSupplier = allItemsSupplier;
        this.invalidIndicateItemsSupplier = invalidIndicateItemsSupplier;
    }

    public void input(String text, String invalidityReason) {
        this.password = text;
        this.isPasswordValid = invalidityReason == null;
        this.invalidityReason = invalidityReason;
    }

    public void updateWindowId(int windowId) {
        if (windowId == 0) return;//it's the player's own inventory
        this.allItemsBuffer = this.allItemsSupplier.apply(windowId);
        this.invalidIndicateItemsBuffer = this.invalidIndicateItemsSupplier.apply(windowId);
        this.spoofValidAccordingly();
    }

    public void spoofAllItems() {
        this.spoof0(allItemsBuffer);
    }

    public void spoofValidAccordingly() {
        this.spoof0(isPasswordValid ? allItemsBuffer : invalidIndicateItemsBuffer);
    }

    public void spoofItemsInvalidIndicate() {
        this.spoof0(invalidIndicateItemsBuffer);
    }

    private void spoof0(ByteBuf packet) {
        NettyUtils.writeAndFlushConst(this.ctx, packet);
    }
}