package shadow.utils.netty.unsafe.first;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;

@Sharable
public abstract class FirstOutboundHandler extends ChannelOutboundHandlerAdapter {

    private final String name;
    //private boolean reAdded;//single thread access - declaring it volatile would be redundant

    public FirstOutboundHandler(String name) {
        this.name = name;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        ctx.pipeline().addFirst(name, this);
    }

    @Override
    public final boolean isSharable() {
        return true;
    }

    //Is it really the best idea to throw an error here?
    /*@ScheduledForFix
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
    }*/
}