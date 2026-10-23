package ua.nanit.limbo.protocol.packets.play.resourcepack;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResourcePackSend;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutResourcePack extends OutRetrooperPacket<WrapperPlayServerResourcePackSend> {

    public PacketPlayOutResourcePack() {
        super(WrapperPlayServerResourcePackSend.class);
    }

    public PacketPlayOutResourcePack(WrapperPlayServerResourcePackSend wrapper) {
        super(wrapper);
    }
}
