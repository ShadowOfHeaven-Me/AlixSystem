package ua.nanit.limbo.protocol.packets.play.map;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutMap extends OutRetrooperPacket<WrapperPlayServerMapData> {

    public PacketPlayOutMap() {
        super(WrapperPlayServerMapData.class);
    }

    public PacketPlayOutMap(WrapperPlayServerMapData wrapper) {
        super(wrapper);
    }
}