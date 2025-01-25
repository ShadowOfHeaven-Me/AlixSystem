package ua.nanit.limbo.protocol.packets.play.slot;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutHeldSlot extends OutRetrooperPacket<WrapperPlayServerHeldItemChange> {

    public PacketPlayOutHeldSlot() {
        super(WrapperPlayServerHeldItemChange.class);
    }

    public PacketOut setSlot(int id) {
        this.wrapper().setSlot(id);
        return this;
    }
}