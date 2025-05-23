package ua.nanit.limbo.protocol.packets.play.ping;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInPong extends InRetrooperPacket<WrapperPlayClientPong> {

    public PacketPlayInPong() {
        super(WrapperPlayClientPong.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}