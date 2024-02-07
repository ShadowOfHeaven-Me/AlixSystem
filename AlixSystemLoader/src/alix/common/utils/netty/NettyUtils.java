package alix.common.utils.netty;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public final class NettyUtils {

    //The following methods prevent StacklessClosedChannelException

    public static void writeAndFlush(Channel channel, Object msg, ChannelFutureListener listener) {
        channel.eventLoop().execute(() -> {
            if (channel.isOpen()) channel.writeAndFlush(msg).addListener(listener);
        });
    }

    public static void writeAndFlush(ChannelHandlerContext ctx, Object msg) {
        ctx.channel().eventLoop().execute(() -> {
            if (ctx.channel().isOpen()) ctx.writeAndFlush(msg);
        });
    }

    public static void writeAndFlush(Channel channel, Object msg) {
        channel.eventLoop().execute(() -> {
            if (channel.isOpen()) channel.writeAndFlush(msg);
        });
    }

    public static void write(Channel channel, Object msg) {
        channel.eventLoop().execute(() -> {
            if (channel.isOpen()) channel.write(msg);
        });
    }

    public static void flush(Channel channel) {
        channel.eventLoop().execute(() -> {
            if (channel.isOpen()) channel.flush();
        });
    }

    private NettyUtils() {
    }
}