package nanolimbo.alix.connection.pipeline.compression;

import alix.common.utils.netty.FastNettyUtils;
import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

final class CompressorJavaImpl implements CompressionImpl {

    private final Deflater deflater;
    private final Inflater inflater;
    private final byte[] encodeBuf;
    private final ByteBufAllocator allocator;

    CompressorJavaImpl(ByteBufAllocator allocator) {
        this.allocator = allocator;
        this.deflater = new Deflater();
        this.deflater.setLevel(Deflater.BEST_COMPRESSION);
        this.inflater = new Inflater();
        this.encodeBuf = new byte[8192];
    }

    @Override
    public void release0() {
        this.deflater.end();
        this.inflater.end();
    }

    @Override
    public ByteBuf compress0(ByteBuf in) {
        int readableBytes = in.readableBytes();

        ByteBuf out = this.allocator.directBuffer();

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
        in.release();
        //Log.error("COMPRESS IN: " + readableBytes + " OUT: " + out.readableBytes() + " REDUCTION: " + ((float) readableBytes / out.readableBytes() - 1) * 100 + "");
        return out;
    }

    @Override
    public ByteBuf decompress0(ByteBuf in, int expectedSize) throws DataFormatException {
        this.setInflaterInput(in);
        ByteBuf out = this.inflate(expectedSize);
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