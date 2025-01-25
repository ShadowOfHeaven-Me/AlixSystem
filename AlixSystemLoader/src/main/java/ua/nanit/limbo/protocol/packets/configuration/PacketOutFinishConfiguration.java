package ua.nanit.limbo.protocol.packets.configuration;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public class PacketOutFinishConfiguration implements PacketOut {

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void encode(ByteMessage msg, Version version) {

    }
}
