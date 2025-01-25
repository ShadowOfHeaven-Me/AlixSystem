package shadow.utils.objects.savable.data.gui.bases;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.misc.packet.buffered.AnvilPacketSupplier;
import shadow.utils.netty.NettyUtils;

public abstract class AnvilBuilderBase {

    private final ChannelHandlerContext ctx;
    private final AnvilPacketSupplier supplier;
    //private ByteBuf allItemsBuffer, invalidIndicateItemsBuffer;
    protected String password, invalidityReason;
    protected boolean isPasswordValid;

    protected AnvilBuilderBase(ChannelHandlerContext ctx, boolean isPasswordValid, AnvilPacketSupplier supplier) {
        this.ctx = ctx;
        this.isPasswordValid = isPasswordValid;
        this.supplier = supplier;
        this.password = supplier.getInput();
    }

    public boolean input(String text) {
        boolean spoof = this.supplier.onInput(text);
        this.password = this.supplier.getInput();
        //Main.debug("input: " + text + " password: " + password);
        return spoof;
    }

    public void updateValidity(String invalidityReason) {
        this.isPasswordValid = invalidityReason == null;
        this.invalidityReason = invalidityReason;
    }

    public String getInput() {
        return this.supplier.getInput();
    }

    public void updateWindowId(int windowId) {
        if (windowId == 0) return;//it's the player's own inventory
        this.supplier.onWindowUpdate(windowId);
        this.spoofValidAccordingly();
    }

    public void spoofAllItems() {
        this.spoof0(this.supplier.allItemsBuffer());
    }

    public void spoofValidAccordingly() {
        this.spoof0(isPasswordValid ? this.supplier.allItemsBuffer() : this.supplier.invalidIndicateBuffer());
    }

    public void spoofItemsInvalidIndicate() {
        this.spoof0(this.supplier.invalidIndicateBuffer());
    }

    private void spoof0(ByteBuf packet) {
        NettyUtils.writeAndFlushConst(this.ctx, packet);
    }
}