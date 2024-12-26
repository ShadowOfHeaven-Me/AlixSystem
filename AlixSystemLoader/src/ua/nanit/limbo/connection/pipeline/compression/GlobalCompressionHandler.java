package ua.nanit.limbo.connection.pipeline.compression;

import io.netty.buffer.ByteBuf;

public final class GlobalCompressionHandler implements CompressionHandler {

    public static final GlobalCompressionHandler INSTANCE = COMPRESSION_ENABLED ? new GlobalCompressionHandler() : null;
    private final CompressionHandlerImpl compress;

    private GlobalCompressionHandler() {
        this.compress = CompressionHandlerImpl.createUnpooledUncached0(CompressionLevel.BEST_COMPRESSION);
    }

    @Override
    public ByteBuf compress(ByteBuf in) throws Exception {
        synchronized (this) {
            return this.compress.compress(in);
        }
    }

    @Override
    public ByteBuf decompress(ByteBuf in) throws Exception {
        synchronized (this) {
            return this.compress.decompress(in);
        }
    }
}
