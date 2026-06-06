package ua.nanit.limbo.protocol.packets.login.cookies;

import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerCookieRequest;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketOutLoginCookieRequest extends OutRetrooperPacket<WrapperLoginServerCookieRequest> {

    public PacketOutLoginCookieRequest() {
        super(WrapperLoginServerCookieRequest.class);
    }

    public PacketOutLoginCookieRequest(byte[] msg) {
        super(WrapperLoginServerCookieRequest.class);

        //this.wrapper().
    }
}