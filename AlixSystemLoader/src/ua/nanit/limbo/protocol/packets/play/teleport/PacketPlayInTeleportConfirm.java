package ua.nanit.limbo.protocol.packets.play.teleport;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInTeleportConfirm extends InRetrooperPacket<WrapperPlayClientTeleportConfirm> {

    public PacketPlayInTeleportConfirm() {
        super(WrapperPlayClientTeleportConfirm.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}