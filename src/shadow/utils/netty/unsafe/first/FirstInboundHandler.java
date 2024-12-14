package shadow.utils.netty.unsafe.first;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public abstract class FirstInboundHandler extends ChannelInboundHandlerAdapter {

    private final String name;
    private boolean allowRemoval;
    //private boolean reAdded;//single thread access - declaring it volatile would be redundant

    public FirstInboundHandler(String name) {
        this.name = name;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (allowRemoval) return;
        ctx.pipeline().addFirst(name, this);
    }

    public void allowRemoval() {
        this.allowRemoval = true;
    }

    @Override
    public final boolean isSharable() {
        return true;
    }

    //Is it really the best idea to throw an error here?
    /*@Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (ctx.pipeline().first() != this) {
            if (this.reAdded) {
                this.reAdded = false;
                return; //give up //throw new AlixException("Circular re-adding!");
            }
            this.reAdded = true;
            ctx.pipeline().remove(name);
            ctx.pipeline().addFirst(name, this);
        }
        this.reAdded = false;
    }*/
}