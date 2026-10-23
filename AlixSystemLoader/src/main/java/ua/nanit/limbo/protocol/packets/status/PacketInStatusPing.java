package ua.nanit.limbo.protocol.packets.status;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public class PacketInStatusPing implements PacketIn {

    private long randomId;

    @Override
    public void decode(ByteMessage msg, Version version) {
        this.randomId = msg.readLong();
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }
}
