package nanolimbo.alix.connection.pipeline.compression;

import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface CompressionHandler {

    ByteBuf compress(ByteBuf in) throws Exception;

    ByteBuf decompress(ByteBuf in) throws Exception;

    int COMPRESSION_THRESHOLD = 128;

    static CompressionHandler getHandler(Channel channel) {
        if (!channel.eventLoop().inEventLoop()) throw new AlixException("Not in eventLoop");
        return CompressionHandlerImpl.getHandler0();
    }

    static void releaseAll() {
        CompressionHandlerImpl.releaseAll();
    }
}