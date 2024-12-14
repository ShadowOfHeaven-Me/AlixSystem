package nanolimbo.alix.protocol.packets.play.transaction;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.retrooper.InRetrooperPacket;
import nanolimbo.alix.server.LimboServer;

public final class PacketPlayInTransaction extends InRetrooperPacket<WrapperPlayClientWindowConfirmation> {

    public PacketPlayInTransaction() {
        super(WrapperPlayClientWindowConfirmation.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}