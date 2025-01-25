package shadow.utils.netty.unsafe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import shadow.utils.netty.unsafe.first.FirstOutboundHandler;

public final class ByteBufHarvester extends FirstOutboundHandler {

    private static final String name = "alix-buf-harvester";
    ByteBuf harvested;
    boolean harvest;

    private ByteBufHarvester() {
        super(name);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (this.harvest) this.harvested = ((ByteBuf) msg).retain();
        super.write(ctx, msg, promise);
    }

    public static ByteBufHarvester newHarvesterFor(Channel channel) {
        ByteBufHarvester harvester = new ByteBufHarvester();
        channel.pipeline().addFirst(name, harvester);
        return harvester;
    }
}