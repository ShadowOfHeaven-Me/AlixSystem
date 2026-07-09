package alix.common.utils.netty.register;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.function.Consumer;

@Sharable
public final class ChannelRegisteredListener extends ChannelInboundHandlerAdapter {

    private final Consumer<ChannelHandlerContext> consumer;

    private ChannelRegisteredListener(Consumer<ChannelHandlerContext> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.consumer.accept(ctx);
        ctx.pipeline().remove(this);
        super.channelRegistered(ctx);
    }

    @Override
    public boolean isSharable() {
        return true;
    }

    public static ChannelRegisteredListener of(Consumer<ChannelHandlerContext> consumer) {
        return new ChannelRegisteredListener(consumer);
    }
}