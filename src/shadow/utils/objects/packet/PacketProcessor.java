package shadow.utils.objects.packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public abstract class PacketProcessor {

    private final PacketInterceptor handler;

    protected PacketProcessor(PacketInterceptor handler) {
        this.handler = handler;
    }

    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        this.handler.channelRead0(ctx, packet);
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        this.handler.write0(ctx, msg, promise);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }
}