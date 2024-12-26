package ua.nanit.limbo.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;

final class ChannelInitImpl {

    private final ChannelHandlerContext silentServerContext;
    private final EventLoopGroup channelGroup;
    private final Channel serverChannel;

    ChannelInitImpl(ChannelHandlerContext silentServerContext) {
        this.silentServerContext = silentServerContext;
        this.serverChannel = silentServerContext.channel();
        this.channelGroup = this.serverChannel.eventLoop().parent();
    }

    void initialChannelRegister(Channel channel) {
        //From ServerBootstrap.ServerBootstrapAcceptor#channelRead
        channelGroup.register(channel).addListener(op -> {
            if (!op.isSuccess()) channel.unsafe().closeForcibly();
            else channel.config().setAutoRead(true);
        });
    }

    ChannelPromise unregister(Channel channel) {
        ChannelPromise promise = channel.newPromise();
        channel.unsafe().deregister(promise);
        return promise;
    }

    public Channel serverChannel() {
        return serverChannel;
    }

    void invokeChannelReadInCtx(Channel channel) {
        this.silentServerContext.fireChannelRead(channel);
    }

    /*private static ChannelHandlerContext findServerBootstrap() {
        ChannelPipeline pipeline = AlixHandler.SERVER_CHANNEL.pipeline();
        for (String handlerName : pipeline.names())
            if (handlerName.startsWith("ServerBootstrap"))
                return pipeline.context(handlerName);
        throw new AlixException("No ServerBootstrap: " + pipeline.names());
    }*/
}