package ua.nanit.limbo.connection;

import alix.common.utils.other.annotation.OptimizationCandidate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.Log;

public final class UnsafeCloseFuture implements ChannelFutureListener {

    public static final UnsafeCloseFuture INSTANCE = new UnsafeCloseFuture();

    private UnsafeCloseFuture() {
    }

    @Override
    public void operationComplete(ChannelFuture f) {
        unsafeClose(f.channel());
    }

    @OptimizationCandidate
    public static void unsafeClose(Channel channel) {
        //channel.unsafe().close(channel.voidPromise());
        //TODO: see if the upper is plausible
        if (NanoLimbo.debugMode) {
            Log.warning("unsafeClose");
            new Exception().printStackTrace();
        }
        channel.close();

        //not sure how to notify the closeFuture() listeners reliably, so let's just do the one above
        /*
        channel.closeFuture().
        channel.unsafe().deregister(channel.voidPromise());
        channel.unsafe().closeForcibly();*/
    }
}