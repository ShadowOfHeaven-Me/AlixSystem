package nanolimbo.alix.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public final class UnsafeCloseFuture implements ChannelFutureListener {

    public static final UnsafeCloseFuture INSTANCE = new UnsafeCloseFuture();

    private UnsafeCloseFuture() {
    }

    @Override
    public void operationComplete(ChannelFuture f) throws Exception {
        unsafeClose(f.channel());
    }

    private static void unsafeClose(Channel channel) {
        channel.unsafe().close(channel.voidPromise());
        //not sure how to notify the closeFuture() listeners reliably, so let's just do the one above
        /*
        channel.closeFuture().
        channel.unsafe().deregister(channel.voidPromise());
        channel.unsafe().closeForcibly();*/
    }
}