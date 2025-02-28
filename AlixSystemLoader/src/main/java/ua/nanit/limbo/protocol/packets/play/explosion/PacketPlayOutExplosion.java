package ua.nanit.limbo.protocol.packets.play.explosion;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutExplosion extends OutRetrooperPacket<WrapperPlayServerExplosion> {

    public PacketPlayOutExplosion() {
        super(WrapperPlayServerExplosion.class);
    }

    public PacketPlayOutExplosion(WrapperPlayServerExplosion wrapper) {
        super(wrapper);
    }
}