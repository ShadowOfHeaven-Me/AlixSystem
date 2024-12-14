package nanolimbo.alix.connection.captcha;

import nanolimbo.alix.protocol.PacketSnapshot;
import nanolimbo.alix.protocol.packets.play.ping.PacketPlayOutPing;

public final class PacketPings {

    //public static final PacketSnapshot[] PINGS = create();
    public static final PacketSnapshot INITIAL_PING = ofId(1); // PINGS[0];

    private static PacketSnapshot[] create() {
        PacketSnapshot[] keepAlives = new PacketSnapshot[1];
        for (int i = 0; i < keepAlives.length; i++) keepAlives[i] = ofId(i + 1);
        return keepAlives;
    }

    private static PacketSnapshot ofId(int id) {
        return PacketSnapshot.of(new PacketPlayOutPing().setId(id));
    }
}