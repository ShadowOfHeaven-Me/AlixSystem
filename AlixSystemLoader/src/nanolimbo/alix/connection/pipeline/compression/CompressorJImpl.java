package nanolimbo.alix.connection.pipeline.compression;

import alix.common.utils.netty.FastNettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import nanolimbo.alix.server.Log;

import java.util.zip.Deflater;

import static nanolimbo.alix.connection.pipeline.compression.CompressionHandler.COMPRESSION_THRESHOLD;

final class CompressorJImpl implements PacketCompressor {

    private final Deflater deflater;
    private final byte[] encodeBuf;
    private final ByteBufAllocator allocator;

    CompressorJImpl(ByteBufAllocator allocator) {
        this.allocator = allocator;
        this.deflater = new Deflater();
        this.encodeBuf = new byte[8192];
    }

    @Override
    public void release() {
        this.deflater.end();
    }

    @Override
    public ByteBuf compress(ByteBuf in) {
        int readableBytes = in.readableBytes();
        if (readableBytes > 1 << 23) Log.warning("Sending packet larger than 2^23 bytes");

        ByteBuf out;
        if (readableBytes < COMPRESSION_THRESHOLD) {
            out = this.allocator.directBuffer(readableBytes + 1);
            out.writeByte(0);//no compression
            out.writeBytes(in);
        } else {
            out = this.allocator.directBuffer();
            byte[] a = new byte[readableBytes];
            in.readBytes(a);
            FastNettyUtils.writeVarInt(out, readableBytes);
            this.deflater.setInput(a, 0, readableBytes);
            this.deflater.finish();

            if (!this.deflater.finished()) {
                int bytes = this.deflater.deflate(this.encodeBuf);
                out.writeBytes(this.encodeBuf, 0, bytes);
            }
            this.deflater.reset();
        }
        in.release();
        return out;
    }
}