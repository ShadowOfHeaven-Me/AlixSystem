package nanolimbo.alix.protocol.packets.configuration;

import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.PacketIn;
import nanolimbo.alix.protocol.registry.Version;
import nanolimbo.alix.server.LimboServer;

public class PacketInFinishConfiguration implements PacketIn {

    @Override
    public void decode(ByteMessage msg, Version version) {

    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}