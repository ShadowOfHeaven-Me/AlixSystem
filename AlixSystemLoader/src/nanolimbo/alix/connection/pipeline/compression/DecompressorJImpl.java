package nanolimbo.alix.connection.pipeline.compression;

import alix.common.utils.netty.FastNettyUtils;
import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

final class DecompressorJImpl implements PacketDecompressor {

    private final Inflater inflater;
    private final ByteBufAllocator allocator;

    DecompressorJImpl(ByteBufAllocator allocator) {
        this.allocator = allocator;
        this.inflater = new Inflater();
    }

    @Override
    public void release() {
        this.inflater.end();
    }

    @Override
    public ByteBuf decompress(ByteBuf in) throws DataFormatException {
        if (!in.isReadable()) return null;

        int length = FastNettyUtils.readVarInt(in);
        if (length == 0) return in;//uncompressed

        this.setInflaterInput(in);
        ByteBuf out = this.inflate(length);
        this.inflater.reset();
        in.release();
        return out;
    }

    private ByteBuf inflate(int expectedSize) throws DataFormatException {
        ByteBuf byteBuf = this.allocator.directBuffer(expectedSize);

        try {
            ByteBuffer byteBuffer = byteBuf.internalNioBuffer(0, expectedSize);
            int i = byteBuffer.position();
            this.inflater.inflate(byteBuffer);
            int j = byteBuffer.position() - i;
            if (j != expectedSize) {
                throw new AlixException("Badly compressed packet - actual length of uncompressed payload " + j + " is does not match declared size " + expectedSize);
            } else {
                byteBuf.writerIndex(byteBuf.writerIndex() + j);
                return byteBuf;
            }
        } catch (Exception var7) {
            byteBuf.release();
            throw var7;
        }
    }

    private void setInflaterInput(ByteBuf buf) {
        ByteBuffer byteBuffer;
        if (buf.nioBufferCount() > 0) {
            byteBuffer = buf.nioBuffer();
            buf.skipBytes(buf.readableBytes());
        } else {
            byteBuffer = ByteBuffer.allocateDirect(buf.readableBytes());
            buf.readBytes(byteBuffer);
            byteBuffer.flip();
        }

        this.inflater.setInput(byteBuffer);
    }
}