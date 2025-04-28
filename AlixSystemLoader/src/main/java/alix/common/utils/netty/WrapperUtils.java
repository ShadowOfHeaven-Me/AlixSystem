package alix.common.utils.netty;

import alix.common.utils.other.AlixUnsafe;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import sun.misc.Unsafe;

public final class WrapperUtils {

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    /*private static final long serverVersionOffset;

    static {
        try {
            serverVersionOffset = UNSAFE.objectFieldOffset(PacketWrapper.class.getDeclaredField("serverVersion"));
        } catch (NoSuchFieldException e) {
            throw new AlixException(e);
        }
    }*/

    @SneakyThrows
    public static <T extends PacketWrapper> T allocEmpty(Class<T> wrapperClazz) {
        return (T) UNSAFE.allocateInstance(wrapperClazz);
    }

    /*public static <T extends PacketWrapper> T initEmpty(ByteBuf buf, ServerVersion version, T wrapper) {
        T wrapper = allocEmpty(wrapperClazz);
        UNSAFE.putObject(wrapper, serverVersionOffset, version);
        wrapper.buffer = buf;
        return wrapper;
    }*/
    public static void setVersion(PacketWrapper<?> wrapper, ServerVersion version) {
        //UNSAFE.putObject(wrapper, serverVersionOffset, version);
        wrapper.setServerVersion(version);
    }

    public static <T extends PacketWrapper> T allocNoBuf(ServerVersion version, Class<T> wrapperClazz) {
        T wrapper = allocEmpty(wrapperClazz);
        setVersion(wrapper, version);
        return wrapper;
    }

    public static <T extends PacketWrapper> T alloc(ByteBuf buf, ServerVersion version, Class<T> wrapperClazz) {
        T wrapper = allocEmpty(wrapperClazz);
        setVersion(wrapper, version);
        wrapper.buffer = buf;
        return wrapper;
    }

    /*public static <T extends PacketWrapper> T allocUnpooled(ServerVersion version, Class<T> wrapperClazz) {
        return alloc(BufUtils.unpooledBuffer(), version, wrapperClazz);
    }*/

    public static <T extends PacketWrapper> void writeWithID(T wrapper, ByteBuf buf, ServerVersion version) {
        setVersion(wrapper, version);
        wrapper.buffer = buf;
        wrapper.writeVarInt(wrapper.getNativePacketId());
        wrapper.write();
    }

    public static <T extends PacketWrapper> void writeNoID(T wrapper, ByteBuf buf, ServerVersion version) {
        setVersion(wrapper, version);
        wrapper.buffer = buf;
        wrapper.write();
    }

    //getPacketType() invoked on the returned object here will be null
    public static <T extends PacketWrapper> void readEmptyWrapperNoID(ByteBuf buf, ServerVersion version, T emptyWrapper) {
        setVersion(emptyWrapper, version);
        emptyWrapper.buffer = buf;
        emptyWrapper.read();
    }

    //getPacketType() invoked on the returned object here will be null
    public static <T extends PacketWrapper> T readNoID(ByteBuf buf, ServerVersion version, Class<T> wrapperClazz) {
        T wrapper = alloc(buf, version, wrapperClazz);
        wrapper.read();
        return wrapper;
    }
}