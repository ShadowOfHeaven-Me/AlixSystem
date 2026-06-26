package ua.nanit.limbo.protocol.packets.login.cookies;

import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientCookieResponse;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;

public final class PacketInLoginCookieResponse extends InRetrooperPacket<WrapperLoginClientCookieResponse> {

    public PacketInLoginCookieResponse() {
        super(WrapperLoginClientCookieResponse.class);
    }

    /*@Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getIntegration().onCookieResponse(conn, this);
    }*/
}