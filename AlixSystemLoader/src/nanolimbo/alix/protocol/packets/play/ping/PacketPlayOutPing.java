package nanolimbo.alix.protocol.packets.play.ping;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import nanolimbo.alix.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutPing extends OutRetrooperPacket<WrapperPlayServerPing> {

    public PacketPlayOutPing() {
        super(WrapperPlayServerPing.class);
    }

    public PacketPlayOutPing setId(int id) {
        this.wrapper().setId(id);
        return this;
    }
}