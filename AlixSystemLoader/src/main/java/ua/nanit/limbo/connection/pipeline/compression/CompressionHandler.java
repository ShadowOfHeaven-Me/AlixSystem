package ua.nanit.limbo.connection.pipeline.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import ua.nanit.limbo.NanoLimbo;

public interface CompressionHandler {

    ByteBuf compress(ByteBuf in) throws Exception;

    ByteBuf decompress(ByteBuf in) throws Exception;

    static ByteBuf decompress(ByteBuf buf, CompressionHandler compress) throws Exception {
        return compress != null ? compress.decompress(buf) : buf;
    }

    boolean __ENABLE_COMPRESS_0 = true;
    int COMPRESSION_THRESHOLD = __ENABLE_COMPRESS_0 ? NanoLimbo.INTEGRATION.getCompressionThreshold() : -1;//-1, since 0 would still be compress enabled
    //A "non-negative" threshold means compression enabled
    // Src: https://minecraft.wiki/w/Java_Edition_protocol/Packets#With_compression
    boolean COMPRESSION_ENABLED = COMPRESSION_THRESHOLD >= 0;

    static CompressionHandler getHandler(Channel channel) {
        return CompressionHandlerImpl.getHandler0(channel);
    }

    static void releaseAll() {
        CompressionHandlerImpl.releaseAll();
    }
}