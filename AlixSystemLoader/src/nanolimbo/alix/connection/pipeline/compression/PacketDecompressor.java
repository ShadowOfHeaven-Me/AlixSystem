package nanolimbo.alix.connection.pipeline.compression;

import io.netty.buffer.ByteBuf;

public interface PacketDecompressor {

    ByteBuf decompress(ByteBuf in) throws Exception;

    void release();

}