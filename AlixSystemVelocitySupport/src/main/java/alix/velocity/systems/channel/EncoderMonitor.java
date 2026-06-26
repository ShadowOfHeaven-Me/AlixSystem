package alix.velocity.systems.channel;

import io.github.retrooper.packetevents.handlers.PacketEventsEncoder;
import io.netty.channel.ChannelHandlerContext;

public final class EncoderMonitor extends PacketEventsEncoder {

    public EncoderMonitor(PacketEventsEncoder original) {
        super(original.user);
        this.player = original.player;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}