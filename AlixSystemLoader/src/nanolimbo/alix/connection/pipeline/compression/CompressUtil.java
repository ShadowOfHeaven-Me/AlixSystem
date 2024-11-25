package nanolimbo.alix.connection.pipeline.compression;

import alix.common.utils.AlixCommonUtils;
import io.netty.buffer.ByteBufAllocator;
import nanolimbo.alix.server.Log;

import java.util.function.Function;

final class CompressUtil {

    private static final Function<ByteBufAllocator, CompressionImpl> factory = createFactoryImpl();

    static CompressionImpl newImpl(ByteBufAllocator allocator) {
        return factory.apply(allocator);
    }

    private static Function<ByteBufAllocator, CompressionImpl> createFactoryImpl() {
        if (AlixCommonUtils.isValidClass("com.velocitypowered.natives.util.Natives")
                && CompressorVelocityImpl.isNativeCompress()) {
            Log.info("Using Velocity Cipher for compression");
            return CompressorVelocityImpl::new;
        }
        Log.info("Using Java for compression");
        return CompressorJavaImpl::new;
    }
}