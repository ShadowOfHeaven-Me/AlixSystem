package nanolimbo.alix.protocol.packets.configuration;

import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.protocol.registry.Version;

public class PacketOutFinishConfiguration implements PacketOut {

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void encode(ByteMessage msg, Version version) {

    }
}
