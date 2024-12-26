package ua.nanit.limbo.handlers;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;

@Sharable
public final class DummyHandler extends ChannelHandlerAdapter {

    public static final DummyHandler HANDLER = new DummyHandler();

    private DummyHandler() {
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}