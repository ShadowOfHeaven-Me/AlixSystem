package nanolimbo.alix.protocol.packets.retrooper;

import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;

abstract class RetrooperPacket<T extends PacketWrapper> {

    RetrooperPacket() {
    }

    public abstract T wrapper();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}