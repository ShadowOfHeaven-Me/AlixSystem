package shadow.utils.objects.filter.packet.check.impl;

import net.minecraft.network.protocol.game.PacketPlayInFlying;
import net.minecraft.network.protocol.game.PacketPlayInKeepAlive;
import shadow.utils.holders.ReflectionUtils;

public final class NewerVersionedPacketAffirmator extends AbstractPacketAffirmator {

    private static final Class<?> serverboundChatCommandPacket;

    static {
        try {
            serverboundChatCommandPacket = Class.forName("net.minecraft.network.protocol.game.ServerboundChatCommandPacket");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public boolean isCommandOrKeepAlive(Object packet) {
        Class<?> clazz = packet.getClass();
        return clazz == serverboundChatCommandPacket || clazz == ReflectionUtils.inKeepAlivePacketClass;
    }

    @Override
    public boolean isKeepAlive(Object packet) {
        return packet.getClass() == PacketPlayInKeepAlive.class;
    }

    @Override
    public boolean isCommand(Object packet) {
        return serverboundChatCommandPacket == packet.getClass();
    }

    @Override
    public boolean isMove(Object packet) {
        return packet instanceof PacketPlayInFlying;
    }

    @Override
    public boolean isCommand(String name) {
        return name.equals("ServerboundChatCommandPacket");
    }
}
