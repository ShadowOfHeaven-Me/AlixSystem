package ua.nanit.limbo.protocol.packets.play.ping;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutPing extends OutRetrooperPacket<WrapperPlayServerPing> {

    public PacketPlayOutPing() {
        super(WrapperPlayServerPing.class);
    }

    public PacketPlayOutPing setId(int id) {
        this.wrapper().setId(id);
        return this;
    }
}