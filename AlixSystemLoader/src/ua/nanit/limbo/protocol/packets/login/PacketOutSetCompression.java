package ua.nanit.limbo.protocol.packets.login;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

import static ua.nanit.limbo.connection.pipeline.compression.CompressionHandler.COMPRESSION_THRESHOLD;

public final class PacketOutSetCompression implements PacketOut {

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeVarInt(COMPRESSION_THRESHOLD);
    }
}