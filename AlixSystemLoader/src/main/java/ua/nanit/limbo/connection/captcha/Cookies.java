package ua.nanit.limbo.connection.captcha;

import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.cookie.PacketPlayOutCookieRequest;
import ua.nanit.limbo.protocol.packets.play.cookie.PacketPlayOutCookieStore;

public final class Cookies {

    public static final String KEY_EMPTY = "0:0";
    public static final String KEY_NULL = "0:1";
    public static final PacketSnapshot COOKIE_STORE_EMPTY = PacketSnapshot.of(new PacketPlayOutCookieStore().setKey(KEY_EMPTY).setPayload(new byte[0]));
    public static final PacketSnapshot COOKIE_REQ_EMPTY = PacketSnapshot.of(new PacketPlayOutCookieRequest().setKey(KEY_EMPTY));
    public static final PacketSnapshot COOKIE_REQ_NULL = PacketSnapshot.of(new PacketPlayOutCookieRequest().setKey(KEY_NULL));

}