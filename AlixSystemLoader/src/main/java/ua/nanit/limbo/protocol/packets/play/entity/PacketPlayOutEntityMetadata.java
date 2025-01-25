package ua.nanit.limbo.protocol.packets.play.entity;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutEntityMetadata extends OutRetrooperPacket<WrapperPlayServerEntityMetadata> {

    public PacketPlayOutEntityMetadata() {
        super(WrapperPlayServerEntityMetadata.class);
    }

    public PacketPlayOutEntityMetadata(WrapperPlayServerEntityMetadata wrapper) {
        super(wrapper);
    }
}