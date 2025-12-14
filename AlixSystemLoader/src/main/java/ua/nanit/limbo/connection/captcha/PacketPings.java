package ua.nanit.limbo.connection.captcha;

import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.ping.PacketPlayOutPing;

public final class PacketPings {

    //public static final PacketSnapshot[] PINGS = create();
    public static final PacketSnapshot INITIAL = ofId(1); // PINGS[0];
    public static final PacketSnapshot SECONDARY = ofId(2);

    /*private static PacketSnapshot[] create() {
        PacketSnapshot[] keepAlives = new PacketSnapshot[1];
        for (int i = 0; i < keepAlives.length; i++) keepAlives[i] = ofId(i + 1);
        return keepAlives;
    }*/

    private static PacketSnapshot ofId(int id) {
        return PacketSnapshot.of(new PacketPlayOutPing().setId(id));
    }
}