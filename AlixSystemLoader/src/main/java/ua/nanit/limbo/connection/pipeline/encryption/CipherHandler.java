package ua.nanit.limbo.connection.pipeline.encryption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;


public interface CipherHandler {

    ByteBuf encrypt(ByteBuf buf) throws Exception;

    ByteBuf decrypt(ByteBuf buf) throws Exception;

    static ByteBuf decrypt(ByteBuf buf, CipherHandler cipher) throws Exception {
        return cipher != null ? cipher.decrypt(buf) : buf;
    }

    static CipherHandler newVelocityHandler(Channel channel) {
        return new VelocityCipherHandler(channel);
    }

    @Deprecated
    private static CipherHandler newUniversalHandler(Channel channel) {
        return new UniversalCipherHandler(channel);
    }
}