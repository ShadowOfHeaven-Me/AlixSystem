package ua.nanit.limbo.connection.captcha;

import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketPlayOutKeepAlive;

public final class KeepAlives {

    //public static final PacketSnapshot[] KEEP_ALIVES = create();

    //!! keep it a 0 !!
    //https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/verification/FallbackPreJoinHandler.java#L85
    public static final int INITIAL_ID = 0;

    public static final int SECONDARY_ID = 1;
    public static final int PREVENT_TIMEOUT_ID = INITIAL_ID;

    public static final PacketSnapshot INITIAL_KEEP_ALIVE = ofId(INITIAL_ID); // KEEP_ALIVES[0];
    public static final PacketSnapshot SECONDARY_KEEP_ALIVE = ofId(SECONDARY_ID);
    public static final PacketSnapshot KEEP_ALIVE_PREVENT_TIMEOUT = INITIAL_KEEP_ALIVE; //ofId(1);

    private static PacketSnapshot[] create() {
        PacketSnapshot[] keepAlives = new PacketSnapshot[5];
        for (int i = 0; i < keepAlives.length; i++) keepAlives[i] = ofId(i + 1);
        return keepAlives;
    }

    private static PacketSnapshot ofId(int id) {
        return PacketSnapshot.of(new PacketPlayOutKeepAlive().setId(id));
    }
}