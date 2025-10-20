package ua.nanit.limbo.connection.pipeline.encryption;

import alix.common.utils.netty.BufUtils;
import alix.common.utils.other.throwable.AlixError;
import com.velocitypowered.natives.Native;
import com.velocitypowered.natives.encryption.VelocityCipher;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

final class VelocityCipherHandler implements CipherHandler {

    private static final Field ENCODE_CIPHER;
    private static final Field DECODE_CIPHER;

    static {
        var path = "com.velocitypowered.proxy.protocol.netty";
        try {
            ENCODE_CIPHER = Class.forName(path + ".MinecraftCipherEncoder").getDeclaredField("cipher");
            ENCODE_CIPHER.setAccessible(true);
            DECODE_CIPHER = Class.forName(path + ".MinecraftCipherDecoder").getDeclaredField("cipher");
            DECODE_CIPHER.setAccessible(true);
        } catch (Exception e) {
            throw new AlixError(e);
        }
    }

    private final VelocityCipher encodeCipher;
    private final VelocityCipher decodeCipher;

    @SneakyThrows
    VelocityCipherHandler(Channel channel) {
        var pipeline = channel.pipeline();

        var encoderCtx = pipeline.context("cipher-encoder");
        var decoderCtx = pipeline.context("cipher-decoder");

        var encoder = (MessageToMessageEncoder<ByteBuf>) encoderCtx.handler();
        var decoder = (MessageToMessageDecoder<ByteBuf>) decoderCtx.handler();

        this.encodeCipher = (VelocityCipher) ENCODE_CIPHER.get(encoder);
        this.decodeCipher = (VelocityCipher) DECODE_CIPHER.get(decoder);
    }

    @Override
    public ByteBuf encrypt(ByteBuf in) {
        ByteBuf compatible = ensureCompatible(BufUtils.POOLED, this.encodeCipher, in);
        try {
            this.encodeCipher.process(compatible);
        } catch (Exception e) {
            compatible.release();
            throw e;
        } finally {
            in.readerIndex(0);
        }
        return compatible;
    }

    @Override
    public ByteBuf decrypt(ByteBuf in) {
        ByteBuf compatible = ensureCompatible(BufUtils.POOLED, this.decodeCipher, in).slice();
        try {
            this.decodeCipher.process(compatible);
        } catch (Exception e) {
            compatible.release();
            throw e;
        } finally {
            in.readerIndex(0);
        }
        return compatible;
    }

    private static ByteBuf ensureCompatible(ByteBufAllocator alloc, Native nativeStuff, ByteBuf buf) {
        if (isCompatible(nativeStuff, buf)) {
            boolean isConst = buf.isReadOnly();
            if (isConst)
                return buf.copy();

            return buf;
        }

        ByteBuf newBuf = MoreByteBufUtils.preferredBuffer(alloc, nativeStuff, buf.readableBytes());
        newBuf.writeBytes(buf);

        buf.release();

        return newBuf;
    }

    private static boolean isCompatible(Native nativeStuff, ByteBuf buf) {
        switch (nativeStuff.preferredBufferType()) {
            case DIRECT_PREFERRED:
            case HEAP_PREFERRED:
                return true;
            case DIRECT_REQUIRED:
                return buf.hasMemoryAddress();
            case HEAP_REQUIRED:
                return buf.hasArray();
            default:
                throw new AssertionError("Preferred buffer type unknown");
        }
    }
}