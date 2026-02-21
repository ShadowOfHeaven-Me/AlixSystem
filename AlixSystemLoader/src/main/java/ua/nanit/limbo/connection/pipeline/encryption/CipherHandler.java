package ua.nanit.limbo.connection.pipeline.encryption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import static ua.nanit.limbo.connection.pipeline.encryption.VelocityCipherHandler.CIPHER_ENCODER_NAME;


public interface CipherHandler {

    ByteBuf encrypt(ByteBuf buf) throws Exception;

    ByteBuf decrypt(ByteBuf buf) throws Exception;

    static ByteBuf decrypt(ByteBuf buf, CipherHandler cipher) throws Exception {
        return cipher != null ? cipher.decrypt(buf) : buf;
    }

    static ByteBuf encrypt(ByteBuf buf, CipherHandler cipher) throws Exception {
        return cipher != null ? cipher.encrypt(buf) : buf;
    }

    static CipherHandler newVelocityHandler(Channel channel) {
        return new VelocityCipherHandler(channel);
    }

    static boolean isEncrypted(Channel channel) {
        var pipeline = channel.pipeline();
        return pipeline.context(CIPHER_ENCODER_NAME) != null;
    }

    static CipherHandler encryptionFor(Channel channel) {
        return isEncrypted(channel) ? newVelocityHandler(channel) : null;
    }

    @Deprecated
    private static CipherHandler newUniversalHandler(Channel channel) {
        return new UniversalCipherHandler(channel);
    }
}