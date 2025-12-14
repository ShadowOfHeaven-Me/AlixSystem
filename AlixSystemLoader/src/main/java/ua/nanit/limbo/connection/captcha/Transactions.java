package ua.nanit.limbo.connection.captcha;

import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.transaction.PacketPlayOutTransaction;

public final class Transactions {

    public static final PacketSnapshot INITIAL = ofId(1);
    public static final PacketSnapshot SECONDARY = ofId(2);

    private static PacketSnapshot ofId(int id) {
        return PacketSnapshot.of(new PacketPlayOutTransaction().setTransactionId(id));
    }
}