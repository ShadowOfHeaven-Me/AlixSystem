package ua.nanit.limbo.protocol.packets.factory.recycler;

import alix.common.utils.other.AlixUnsafe;
import sun.misc.Unsafe;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;

public final class PacketRecycler {

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    private final boolean hasWrapper;

    public PacketRecycler(Class<? extends Packet> packet) {
        this.hasWrapper = InRetrooperPacket.class.isAssignableFrom(packet);

        //todo: impl recycling for PacketWrappers & normal objects (either via Unsafe or just manual methods for all impls)
        //UNSAFE.setMemory();
    }

    public static boolean isAvailable() {
        return AlixUnsafe.hasUnsafe();
    }
}