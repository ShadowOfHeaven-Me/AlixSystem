package ua.nanit.limbo.protocol.packets.play.cookie;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCookieRequest;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutCookieRequest extends OutRetrooperPacket<WrapperPlayServerCookieRequest> {

    public PacketPlayOutCookieRequest() {
        super(WrapperPlayServerCookieRequest.class);
    }

    public PacketPlayOutCookieRequest setKey(String key) {
        String[] w = key.split(":");
        this.wrapper().setKey(new ResourceLocation(w[0], w[1]));
        return this;
    }
}