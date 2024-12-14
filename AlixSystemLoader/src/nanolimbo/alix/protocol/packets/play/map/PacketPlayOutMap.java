package nanolimbo.alix.protocol.packets.play.map;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData;
import nanolimbo.alix.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutMap extends OutRetrooperPacket<WrapperPlayServerMapData> {

    public PacketPlayOutMap() {
        super(WrapperPlayServerMapData.class);
    }

    public PacketPlayOutMap(WrapperPlayServerMapData wrapper) {
        super(wrapper);
    }
}