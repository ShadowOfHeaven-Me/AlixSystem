package shadow.utils.netty.unsafe.first;

import alix.common.utils.other.annotation.ScheduledForFix;
import alix.common.utils.other.throwable.AlixException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;

public abstract class FirstOutboundHandler extends ChannelOutboundHandlerAdapter {

    private final String name;
    private boolean reAdded;//single thread access - declaring it volatile would be redundant

    public FirstOutboundHandler(String name) {
        this.name = name;
    }

    //Is it really the best idea to throw an error here?
    @ScheduledForFix
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