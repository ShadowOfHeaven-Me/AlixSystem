package ua.nanit.limbo.connection.pipeline.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import ua.nanit.limbo.NanoLimbo;

public interface CompressionHandler {

    ByteBuf compress(ByteBuf in) throws Exception;

    ByteBuf decompress(ByteBuf in) throws Exception;

    int COMPRESSION_THRESHOLD = NanoLimbo.INTEGRATION.getCompressionThreshold();
    boolean COMPRESSION_ENABLED = COMPRESSION_THRESHOLD > 0;

    static CompressionHandler getHandler(Channel channel) {
        return CompressionHandlerImpl.getHandler0(channel);
    }

    static void releaseAll() {
        CompressionHandlerImpl.releaseAll();
    }

}