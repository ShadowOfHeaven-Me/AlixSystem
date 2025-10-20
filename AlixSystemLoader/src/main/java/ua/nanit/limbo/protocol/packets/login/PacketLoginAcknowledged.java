package ua.nanit.limbo.protocol.packets.login;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public final class PacketLoginAcknowledged implements PacketIn {

    public static final PacketLoginAcknowledged INSTANCE = new PacketLoginAcknowledged();

    @Override
    public void decode(ByteMessage msg, Version version) {
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }

    private PacketLoginAcknowledged() {
    }
}
