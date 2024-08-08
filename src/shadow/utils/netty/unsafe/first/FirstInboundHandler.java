package shadow.utils.netty.unsafe.first;

import alix.common.utils.other.throwable.AlixException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class FirstInboundHandler extends ChannelInboundHandlerAdapter {

    private final String name;
    private boolean reAdded;//single thread access - declaring it volatile would be redundant

    public FirstInboundHandler(String name) {
        this.name = name;
    }

    @Override
    public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        if (ctx.pipeline().first() != this) {
            if (this.reAdded) throw new AlixException("Circular re-adding!");
            this.reAdded = true;
            ctx.pipeline().remove(name);
            ctx.pipeline().addFirst(name, this);
        }
        this.reAdded = false;
    }
}