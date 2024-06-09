package shadow.systems.virtualization.packets.login_impl;

import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketTypeData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.AlixUnsafe;
import sun.misc.Unsafe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class PacketTransformation {

/*    public static ByteBuf createLogin() throws Exception {
        Class<?> mcServerClass = ReflectionUtils.nms2("server.MinecraftServer");
        Object mcServer = mcServerClass.getMethod("getServer").invoke(null);

        ByteBuf buf = packetDataSerializerOf(Unpooled.buffer());
        Class<?> clazz = ReflectionUtils.nms2("network.protocol.game.PacketPlayOutLogin");
        for (Constructor<?> constructor : clazz.getConstructors()) {

        }
        *//*switch (PacketEvents.getAPI().getServerManager().getVersion()) {
            case V_1_20_1:

        }*//*
        return buf;
    }*/

/*    public static PacketWrapper<?> wrapperOf(Object minecraftPacket) {
        ByteBuf buf = write(minecraftPacket);
    }*/

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    private static final long packetTypeDataOffset = AlixUnsafe.objectFieldOffset(PacketWrapper.class, "packetTypeData");

    public static <T extends PacketWrapper> T writeWrapperContents(Class<T> wrapperClazz, ByteBuf contents, PacketTypeCommon type, int packetId) throws Exception {
        PacketWrapper wrapper = (PacketWrapper) UNSAFE.allocateInstance(wrapperClazz);
        wrapper.buffer = contents;
        UNSAFE.putObject(wrapper, packetTypeDataOffset, new PacketTypeData(type, packetId));
        wrapper.setServerVersion(PacketEvents.getAPI().getServerManager().getVersion());
        wrapper.read();
        contents.release();
        wrapper.buffer = null;
        return (T) wrapper;
    }

    public static int getPacketId(Object packet) throws InvocationTargetException, IllegalAccessException {
        //https://mappings.cephx.dev/1.20.4/net/minecraft/network/ConnectionProtocol.html
        Enum playEnumProtocol = (Enum) ReflectionUtils.nms2("network.EnumProtocol").getEnumConstants()[1];
        Method packetIdMethod = ReflectionUtils.getMethodByReturnType(playEnumProtocol.getClass(), int.class, ReflectionUtils.enumProtocolDirectionClazz, ReflectionUtils.packetClazz);

        return (int) packetIdMethod.invoke(playEnumProtocol, ReflectionUtils.clientboundProtocolDirection, packet);
    }

    //excludes the packet id from the buffer
    public static ByteBuf getContentsOf(Object packet) {
        ByteBuf buf = newPacketDataSerializer();

        try {
            Method method = ReflectionUtils.getMethodByReturnType(ReflectionUtils.packetClazz, void.class, ReflectionUtils.packetDataSerializerClass);
            //write packet's contents
            method.invoke(packet, buf);
            return buf;
        } catch (Throwable e) {
            throw new AlixException(packet.getClass().getSimpleName(), e);
        }
    }

    public static ByteBuf newPacketDataSerializer() {
        return packetDataSerializerOf(Unpooled.buffer());
    }

    public static ByteBuf packetDataSerializerOf(ByteBuf buf) {
        try {
            return ReflectionUtils.packetDataSerializerConstructor.newInstance(buf);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}