package ua.nanit.limbo.protocol.packets.play.inventory;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutSetSlot extends OutRetrooperPacket<WrapperPlayServerSetSlot> {

    public PacketPlayOutSetSlot() {
        super(WrapperPlayServerSetSlot.class);
    }

    public PacketPlayOutSetSlot(WrapperPlayServerSetSlot wrapper) {
        super(wrapper);
    }
}