package alix.common.utils.netty;

import alix.common.utils.other.AlixUnsafe;
import alix.common.utils.other.throwable.AlixException;
import alix.libs.com.github.retrooper.packetevents.manager.server.ServerVersion;
import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import sun.misc.Unsafe;

public final class WrapperUtils {

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    private static final long serverVersionOffset;

    static {
        try {
            serverVersionOffset = UNSAFE.objectFieldOffset(PacketWrapper.class.getDeclaredField("serverVersion"));
        } catch (NoSuchFieldException e) {
            throw new AlixException(e);
        }
    }

    public static <T extends PacketWrapper> T writeNoID(ByteBuf buf, T wrapper, ServerVersion version) {
        UNSAFE.putObject(wrapper, serverVersionOffset, version);
        wrapper.buffer = buf;
        wrapper.write();
        return wrapper;
    }

    //getPacketType() invoked on the returned object here will be null
    public static <T extends PacketWrapper> T readNoID(ByteBuf buf, ServerVersion version, Class<T> wrapperClazz) {
        try {
            T wrapper = (T) UNSAFE.allocateInstance(wrapperClazz);
            UNSAFE.putObject(wrapper, serverVersionOffset, version);
            wrapper.buffer = buf;
            wrapper.read();
            return wrapper;
        } catch (InstantiationException e) {
            throw new AlixException(e);
        }
    }
}