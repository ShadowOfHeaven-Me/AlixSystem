package ua.nanit.limbo.protocol.packets.configuration;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketOutFinishConfiguration implements PacketOut {

    public static final PacketOutFinishConfiguration INSTANCE = new PacketOutFinishConfiguration();

    @Override
    public void encode(ByteMessage msg, Version version) {
    }
}
