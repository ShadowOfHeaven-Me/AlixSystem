package nanolimbo.alix.connection.pipeline.compression;

import io.netty.buffer.ByteBuf;

interface PacketCompressor {

    ByteBuf compress(ByteBuf in) throws Exception;

    void release();

}