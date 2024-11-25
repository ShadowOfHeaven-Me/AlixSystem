package nanolimbo.alix.connection.pipeline.compression;

import io.netty.buffer.ByteBuf;

interface CompressionImpl {

    //actually does the compression
    ByteBuf compress0(ByteBuf in) throws Exception;

    //actually does the decompression
    ByteBuf decompress0(ByteBuf in, int expectedSize) throws Exception;

    void release0();
}