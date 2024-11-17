package nanolimbo.alix.protocol.packets.configuration;

import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.PacketIn;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.server.LimboServer;

public class PacketFinishConfiguration implements PacketIn, PacketOut {

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
