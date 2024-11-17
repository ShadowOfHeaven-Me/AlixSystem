package nanolimbo.alix.connection.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;

public final class NoTimeoutHandler extends ChannelHandlerAdapter {

    private final ChannelHandler originalTimeOut;

    public NoTimeoutHandler(ChannelHandler originalTimeOut) {
        this.originalTimeOut = originalTimeOut;
    }
}