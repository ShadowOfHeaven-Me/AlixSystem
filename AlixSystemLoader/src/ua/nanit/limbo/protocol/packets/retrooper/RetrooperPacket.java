package ua.nanit.limbo.protocol.packets.retrooper;

import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ua.nanit.limbo.protocol.Packet;

abstract class RetrooperPacket<T extends PacketWrapper> implements Packet {

    RetrooperPacket() {
    }

    public abstract T wrapper();

    @Override
    public final String toString() {
        return this.getClass().getSimpleName();
    }
}