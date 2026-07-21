package ua.nanit.limbo.connection.pipeline.encryption;

import alix.common.environment.ServerEnvironment;
import alix.common.utils.other.throwable.AlixError;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;


public interface CipherHandler {

    ByteBuf encrypt(ByteBuf buf) throws Exception;

    ByteBuf decrypt(ByteBuf buf) throws Exception;

    static ByteBuf decrypt(ByteBuf buf, CipherHandler cipher) throws Exception {
        return cipher != null ? cipher.decrypt(buf) : buf;
    }

    static ByteBuf encrypt(ByteBuf buf, CipherHandler cipher) throws Exception {
        return cipher != null ? cipher.encrypt(buf) : buf;
    }

    static boolean isEncrypted(Channel channel) {
        var pipeline = channel.pipeline();
        return pipeline.context(UniversalCipherHandler.encoderName()) != null;
    }

    static CipherHandler encryptionFor(Channel channel) {
        return isEncrypted(channel) ? newHandler(channel) : null;
    }

    private static CipherHandler newHandler(Channel channel) {
        return switch (ServerEnvironment.getEnvironment()) {
            case VELOCITY -> newVelocityHandler(channel);
            case SPIGOT, PAPER -> newUniversalHandler(channel);
            default -> throw new AlixError();
        };
    }

    private static CipherHandler newVelocityHandler(Channel channel) {
        return new VelocityCipherHandler(channel);
    }

    private static CipherHandler newUniversalHandler(Channel channel) {
        return new UniversalCipherHandler(channel);
    }
}