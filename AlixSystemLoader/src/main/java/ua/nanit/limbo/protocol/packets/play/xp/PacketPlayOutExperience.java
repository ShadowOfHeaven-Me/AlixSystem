package ua.nanit.limbo.protocol.packets.play.xp;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetExperience;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutExperience extends OutRetrooperPacket<WrapperPlayServerSetExperience> {

    public PacketPlayOutExperience() {
        super(WrapperPlayServerSetExperience.class);
    }

    public PacketPlayOutExperience(WrapperPlayServerSetExperience wrapper) {
        super(wrapper);
    }
}