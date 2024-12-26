package ua.nanit.limbo.connection.pipeline.compression;

import alix.common.utils.AlixCommonUtils;
import io.netty.buffer.ByteBufAllocator;
import ua.nanit.limbo.server.Log;

final class CompressUtil {

    private static final boolean useVelocityCompress = false;//currently disabled
    private static final CompressImplFactory factory = createFactoryImpl0();

    static CompressionImpl newImpl(ByteBufAllocator allocator, CompressionLevel level) {
        return factory.newCompress(allocator, level);
    }

    private static CompressImplFactory createFactoryImpl0() {
        if (useVelocityCompress && AlixCommonUtils.isValidClass("com.velocitypowered.natives.util.Natives")
                && CompressorVelocityImpl.isNativeCompress()) {
            Log.info("Using Velocity Cipher for compression");
            return CompressorVelocityImpl::new;
        }
        Log.info("Using Java for compression");
        return CompressorJavaImpl::new;
    }

    private interface CompressImplFactory {

        CompressionImpl newCompress(ByteBufAllocator alloc, CompressionLevel lvl);

    }
}