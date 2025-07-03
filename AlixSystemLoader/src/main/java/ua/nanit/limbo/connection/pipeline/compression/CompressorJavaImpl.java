package ua.nanit.limbo.connection.pipeline.compression;

import alix.common.utils.netty.FastNettyUtils;
import alix.common.utils.netty.NettySafety;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

final class CompressorJavaImpl implements CompressionImpl {

    //Source code: https://github.com/PaperMC/Velocity/blob/d77e508e9cb65894848f4dfbcf3f7c9341d64225/native/src/main/java/com/velocitypowered/natives/compression/JavaVelocityCompressor.java

    //private static final int BUF_SIZE = 32768;
    private final Deflater deflater;
    private final Inflater inflater;
    private final byte[] encodeBuf;
    private final ByteBufAllocator allocator;

    CompressorJavaImpl(ByteBufAllocator allocator, CompressionLevel level) {
        this.allocator = allocator;
        this.deflater = new Deflater();
        this.deflater.setLevel(level.getJavaLevel());
        this.inflater = new Inflater();
        this.encodeBuf = new byte[NettySafety.MAX_RECEIVED_SIZE];
    }

    @Override
    public void release0() {
        this.deflater.end();
        this.inflater.end();
    }

/*    @Override
    public ByteBuf compress0(ByteBuf in) {
        if (in.nioBufferCount() != 1)
            throw new AlixException("I have no idea what I'm doing. IN: " + in.nioBufferCount());

        ByteBuf out = this.allocator.directBuffer();

        if (out.nioBufferCount() != 1)
            throw new AlixException("I have no idea what I'm doing. OUT: " + out.nioBufferCount());

        //int originalReaderIdx = in.readerIndex();

        this.deflater.setInput(in.nioBuffer());
        this.deflater.finish();

        while (!this.deflater.finished()) {
            if (!out.isWritable()) out.ensureWritable(BUF_SIZE);

            ByteBuffer outNio = out.nioBuffer(out.writerIndex(), out.writableBytes());
            int compressed = this.deflater.deflate(outNio);
            out.writerIndex(out.writerIndex() + compressed);
        }

        //in.readerIndex(originalReaderIdx + this.deflater.getTotalIn());
        in.release();
        this.deflater.reset();

        return out;
    }*/

    @Override
    public ByteBuf compress0(ByteBuf in) {
        int readableBytes = in.readableBytes();
        //if (readableBytes > this.encodeBuf.length) throw new AlixError("LEN: " + readableBytes + " ENC: " + this.encodeBuf.length);

        ByteBuf out = this.allocator.directBuffer();

        byte[] a = new byte[readableBytes];
        in.readBytes(a);
        FastNettyUtils.writeVarInt(out, readableBytes);
        this.deflater.setInput(a, 0, readableBytes);
        this.deflater.finish();

        if (!this.deflater.finished()) {
            int read = this.deflater.deflate(this.encodeBuf);
            if (read == this.encodeBuf.length)
                throw new AlixError("LEN: " + readableBytes + " ENC: " + this.encodeBuf.length);
            out.writeBytes(this.encodeBuf, 0, read);
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