package ua.nanit.limbo.connection.pipeline.compression;

import io.netty.buffer.ByteBuf;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;

public final class GlobalCompressionHandler implements CompressionHandler {

    public static final GlobalCompressionHandler INSTANCE = COMPRESSION_ENABLED ? new GlobalCompressionHandler() : null;
    private final CompressionHandlerImpl compress;

    private GlobalCompressionHandler() {
        this.compress = CompressionHandlerImpl.createUnpooledUncached0(CompressionLevel.BEST_COMPRESSION);
    }

    public static CompressionHandler getCompressionFor(Packet packet, Version version, State state) {
        if (CompressionSupplier.compressDisabled(packet, version, state))
            return null;

        return INSTANCE;
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
