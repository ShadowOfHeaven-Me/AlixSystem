package nanolimbo.alix.connection.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;

@ChannelHandler.Sharable
public final class NoTimeoutHandler extends ChannelHandlerAdapter {

    public static final NoTimeoutHandler INSTANCE = new NoTimeoutHandler();

    /*private final ChannelHandler originalTimeOut;

    public NoTimeoutHandler(ChannelHandler originalTimeOut) {
        this.originalTimeOut = originalTimeOut;
    }*/

    private NoTimeoutHandler() {
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}