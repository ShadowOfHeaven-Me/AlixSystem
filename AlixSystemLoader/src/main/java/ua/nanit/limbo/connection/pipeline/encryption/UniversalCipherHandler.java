package ua.nanit.limbo.connection.pipeline.encryption;

import alix.common.environment.ServerEnvironment;
import alix.common.reflection.CommonReflection;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.common.utils.other.throwable.AlixError;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.lang.reflect.InvocationTargetException;
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

    //write a proper optimization for this...
    @OptimizationCandidate
    private static ByteBuf compatible(ByteBuf buf) {
        return buf.isReadOnly() ? buf.copy() : buf;
    }

    static ByteBuf crypt(Method method, ChannelHandlerAdapter handler, ChannelHandlerContext ctx, ByteBuf in, List<ByteBuf> out) throws InvocationTargetException, IllegalAccessException {
        ByteBuf compatible = compatible(in);

        if (compatible != in) {
            //compatible is a fresh copy
            in.release();
        }

        method.invoke(handler, ctx, compatible, out);

        return result(out);
    }

    @Override
    public ByteBuf encrypt(ByteBuf in) throws Exception {
        return crypt(ENCODE, this.encoder, this.encoderCtx, in, this.out);
    }

    @Override
    public ByteBuf decrypt(ByteBuf in) throws Exception {
        ByteBuf in0 = compatible(in);

        return crypt(DECODE, this.decoder, this.decoderCtx, in0, this.out);
    }

    static String encoderName() {
        return switch (ServerEnvironment.getEnvironment()) {
            case VELOCITY -> "cipher-encoder";
            case PAPER, SPIGOT -> "encrypt";
            default -> throw new AlixError("ServerEnvironment.getEnvironment()=" + ServerEnvironment.getEnvironment());
        };
    }

    static String decoderName() {
        return switch (ServerEnvironment.getEnvironment()) {
            case VELOCITY -> "cipher-decoder";
            case PAPER, SPIGOT -> "decrypt";
            default -> throw new AlixError("ServerEnvironment.getEnvironment()=" + ServerEnvironment.getEnvironment());
        };
    }
}