package nanolimbo.alix.connection.pipeline.compression;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

public final class CompressionHandler {

    public static final int COMPRESSION_THRESHOLD = 256;
    private final PacketCompressor compressor;
    private final PacketDecompressor decompressor;

    public CompressionHandler(Channel channel) {
        ByteBufAllocator alloc = channel.alloc();
        this.compressor = new CompressorJImpl(alloc);
        this.decompressor = new DecompressorJImpl(alloc);

        channel.closeFuture().addListener(f -> this.release());
    }

    public ByteBuf compress(ByteBuf in) throws Exception {
        return this.compressor.compress(in);
    }

    public ByteBuf decompress(ByteBuf in) throws Exception {
        return this.decompressor.decompress(in);
    }

    private void release() {
        this.compressor.release();
        this.decompressor.release();
    }
}