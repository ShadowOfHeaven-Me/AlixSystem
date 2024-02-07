package shadow.utils.objects.packet.types.verified;

import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.objects.packet.PacketProcessor;

public final class SelfDelegatingProcessor extends PacketProcessor {

    public SelfDelegatingProcessor(PacketInterceptor handler) {
        super(handler);
    }
/*    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        super.channelRead(ctx, packet);
        Main.logInfo("BEFORE INJECTION: CLIENT " + packet.getClass().getSimpleName());
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        Main.logInfo("BEFORE INJECTION: SERVER " + msg.getClass().getSimpleName());
    }*/
}