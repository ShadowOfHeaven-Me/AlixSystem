package ua.nanit.limbo.connection.pipeline.encryption;

import alix.common.environment.ServerEnvironment;
import alix.common.reflection.CommonReflection;
import alix.common.utils.other.throwable.AlixError;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

final class UniversalCipherHandler implements CipherHandler {

    private static final Method ENCODE = CommonReflection.getMethod(MessageToMessageEncoder.class, "encode", ChannelHandlerContext.class, Object.class, List.class);
    private static final Method DECODE = CommonReflection.getMethod(MessageToMessageDecoder.class, "decode", ChannelHandlerContext.class, Object.class, List.class);

    private final ChannelHandlerContext encoderCtx;
    private final ChannelHandlerContext decoderCtx;
    private final MessageToMessageEncoder<ByteBuf> encoder;
    private final MessageToMessageDecoder<ByteBuf> decoder;

    private final List<ByteBuf> out;

    UniversalCipherHandler(Channel channel) {
        this.out = new ArrayList<>(1);
        var pipeline = channel.pipeline();
        this.encoderCtx = pipeline.context(encoderName());
        this.decoderCtx = pipeline.context(decoderName());
        this.encoder = (MessageToMessageEncoder<ByteBuf>) this.encoderCtx.handler();
        this.decoder = (MessageToMessageDecoder<ByteBuf>) this.decoderCtx.handler();
    }

    private static ByteBuf result(List<ByteBuf> out) {
        int size = out.size();
        if (size == 0)
            throw new AlixError("Wtf");

        //should always be 1
        var buf = size == 1 ? out.get(0) : Unpooled.wrappedBuffer(out.toArray(new ByteBuf[0]));
        out.clear();
        return buf;
    }

    @Override
    public ByteBuf encrypt(ByteBuf in) throws Exception {
        ENCODE.invoke(this.encoder, this.encoderCtx, in, this.out);
        return result(this.out);
    }

    @Override
    public ByteBuf decrypt(ByteBuf in) throws Exception {
        DECODE.invoke(this.decoder, this.decoderCtx, in, this.out);
        return result(this.out);
    }

    private static String encoderName() {
        return switch (ServerEnvironment.getEnvironment()) {
            case VELOCITY -> "cipher-encoder";
            default -> throw new AlixError("ServerEnvironment.getEnvironment()=" + ServerEnvironment.getEnvironment());
        };
    }

    private static String decoderName() {
        return switch (ServerEnvironment.getEnvironment()) {
            case VELOCITY -> "cipher-decoder";
            default -> throw new AlixError("ServerEnvironment.getEnvironment()=" + ServerEnvironment.getEnvironment());
        };
    }
}