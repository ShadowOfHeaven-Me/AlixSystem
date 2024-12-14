package nanolimbo.alix.protocol.packets.play.ping;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.retrooper.InRetrooperPacket;
import nanolimbo.alix.server.LimboServer;

public final class PacketPlayInPong extends InRetrooperPacket<WrapperPlayClientPong> {

    public PacketPlayInPong() {
        super(WrapperPlayClientPong.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}