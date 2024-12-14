package nanolimbo.alix.connection.pipeline.compression;

import alix.common.utils.other.throwable.AlixError;
import com.velocitypowered.natives.Native;
import com.velocitypowered.natives.compression.JavaVelocityCompressor;
import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.compression.VelocityCompressorFactory;
import com.velocitypowered.natives.util.BufferPreference;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import nanolimbo.alix.server.Log;

final class CompressorVelocityImpl implements CompressionImpl {

    private static final VelocityCompressorFactory factory = Natives.compress.get();
    private final ByteBufAllocator allocator;
    private final VelocityCompressor compressor;

    CompressorVelocityImpl(ByteBufAllocator allocator) {
        this.allocator = allocator;
        this.compressor = factory.create(-1);
    }

   /* private static ByteBufAllocator getCompatibleAlloc(ByteBufAllocator allocator) {

    }*/

    @Override
    public ByteBuf compress0(ByteBuf in) throws Exception {
        int readableBytes = in.readableBytes();

        //Can isCompatible ever even fail here?
        ByteBuf out = ensureCompatible(this.allocator, this.compressor, MoreByteBufUtils.preferredBuffer(this.allocator, this.compressor, readableBytes + 1));
        this.compressor.deflate(in, out);

        return out;
    }

    @Override
    public ByteBuf decompress0(ByteBuf in, int expectedSize) throws Exception {
        ByteBuf compatibleSrc = ensureCompatible(this.allocator, this.compressor, in);
        ByteBuf out = MoreByteBufUtils.preferredBuffer(this.allocator, this.compressor, expectedSize);

        this.compressor.inflate(compatibleSrc, out, expectedSize);
        //compatibleSrc.release();

        return out;
    }

    @Override
    public void release0() {
        this.compressor.close();
    }

    //https://github.com/PaperMC/Velocity/blob/dev/3.0.0/native/src/main/java/com/velocitypowered/natives/util/MoreByteBufUtils.java

    //ensureCompatible(), but without a retain() call, and instead with a release() call
    private static ByteBuf ensureCompatible(ByteBufAllocator alloc, Native nativeStuff, ByteBuf buf) {
        if (isCompatible(nativeStuff, buf)) return buf;

        //throw new AlixError("INCOMPATIBLE: GOT: " + buf + " PREFERRED: " + nativeStuff.preferredBufferType());
        Log.error("INCOMPATIBLE: GOT: " + buf + " PREFERRED: " + nativeStuff.preferredBufferType());

        ByteBuf newBuf = MoreByteBufUtils.preferredBuffer(alloc, nativeStuff, buf.readableBytes());
        newBuf.writeBytes(buf);

        buf.release();

        return newBuf;
    }

    private static boolean isCompatible(Native nativeStuff, ByteBuf buf) {
        BufferPreference preferred = nativeStuff.preferredBufferType();
        switch (preferred) {
            case DIRECT_PREFERRED:
            case HEAP_PREFERRED:
                return true;
            case DIRECT_REQUIRED:
                return buf.hasMemoryAddress();
            case HEAP_REQUIRED:
                return buf.hasArray();
            default:
                throw new AlixError("Invalid preferred buffer type: " + preferred);
        }
    }

    //https://github.com/PaperMC/Velocity/blob/dev/3.0.0/native/src/main/java/com/velocitypowered/natives/util/Natives.java
    static boolean isNativeCompress() {
        return factory != JavaVelocityCompressor.FACTORY;
    }
}