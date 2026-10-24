package ua.nanit.limbo.protocol.packets.play.resourcepack;

import alix.common.data.fingerprinting.FingerprintGateway;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientResourcePackStatus;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInResourcePackResponse extends InRetrooperPacket<WrapperPlayClientResourcePackStatus> {

    public PacketPlayInResourcePackResponse() {
        super(WrapperPlayClientResourcePackStatus.class);
    }

    public PacketPlayInResourcePackResponse(WrapperPlayClientResourcePackStatus wrapper) {
        super(wrapper);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        FingerprintGateway.onResourcePackStatus(conn.getChannel(), this.wrapper().getPackId(), this.wrapper().getResult());
    }
}
