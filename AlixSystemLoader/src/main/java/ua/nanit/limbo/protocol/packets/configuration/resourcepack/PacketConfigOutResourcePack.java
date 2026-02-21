package ua.nanit.limbo.protocol.packets.configuration.resourcepack;

import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerResourcePackSend;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketConfigOutResourcePack extends OutRetrooperPacket<WrapperConfigServerResourcePackSend> {

    public PacketConfigOutResourcePack() {
        super(WrapperConfigServerResourcePackSend.class);
    }

    public PacketConfigOutResourcePack(WrapperConfigServerResourcePackSend wrapper) {
        super(wrapper);
    }
}