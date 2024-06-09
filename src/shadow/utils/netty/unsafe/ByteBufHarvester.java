package shadow.utils.netty.unsafe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public final class ByteBufHarvester extends ChannelOutboundHandlerAdapter {

    private static final String name = "alix-harvester";
    volatile ByteBuf harvested;
    volatile boolean harvest;

    private ByteBufHarvester() {
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!this.harvest) super.write(ctx, msg, promise);
        else this.harvested = (ByteBuf) msg;
    }

    public static ByteBufHarvester newHarvesterFor(Channel channel) {
        ByteBufHarvester harvester = new ByteBufHarvester();
        channel.pipeline().addFirst(name, harvester);
        return harvester;
    }
}