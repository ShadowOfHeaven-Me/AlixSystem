package nanolimbo.alix.protocol.packets.play.teleport;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.retrooper.InRetrooperPacket;
import nanolimbo.alix.server.LimboServer;

public final class PacketPlayInTeleportConfirm extends InRetrooperPacket<WrapperPlayClientTeleportConfirm> {

    public PacketPlayInTeleportConfirm() {
        super(WrapperPlayClientTeleportConfirm.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}