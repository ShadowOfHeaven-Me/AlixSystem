package ua.nanit.limbo.protocol.packets.play.cookie;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerStoreCookie;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

import java.util.function.Supplier;

public final class PacketPlayOutCookieStore extends OutRetrooperPacket<WrapperPlayServerStoreCookie> {

    public PacketPlayOutCookieStore() {
        super(WrapperPlayServerStoreCookie.class);
    }

    public PacketPlayOutCookieStore setKey(String key) {
        String[] w = key.split(":");
        this.wrapper().setKey(new ResourceLocation(w[0], w[1]));
        return this;
    }
    public PacketPlayOutCookieStore setPayload(byte[] payload) {
        this.wrapper().setPayload(payload);
        return this;
    }

    public PacketPlayOutCookieStore setPayload(Supplier<byte[]> payload) {
        this.wrapper().setPayload(payload.get());
        return this;
    }
}