package ua.nanit.limbo.protocol.packets.play.config;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerConfigurationStart;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutReconfigure extends OutRetrooperPacket<WrapperPlayServerConfigurationStart> {

    public PacketPlayOutReconfigure() {
        super(WrapperPlayServerConfigurationStart.class);
    }
}