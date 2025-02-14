package ua.nanit.limbo.protocol.packets.play.cookie;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCookieResponse;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInCookieResponse extends InRetrooperPacket<WrapperPlayClientCookieResponse> {

    public PacketPlayInCookieResponse() {
        super(WrapperPlayClientCookieResponse.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}