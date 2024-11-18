package nanolimbo.alix.protocol.packets.login;

import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.protocol.registry.Version;

import static nanolimbo.alix.connection.pipeline.compression.CompressionHandler.COMPRESSION_THRESHOLD;

public final class PacketInSetCompression implements PacketOut {

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeVarInt(COMPRESSION_THRESHOLD);
    }
}