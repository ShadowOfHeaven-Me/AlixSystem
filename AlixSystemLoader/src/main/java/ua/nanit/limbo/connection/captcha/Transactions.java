package ua.nanit.limbo.connection.captcha;

import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.transaction.PacketPlayOutTransaction;

public final class Transactions {

    public static final PacketSnapshot VALID = ofId(1);

    private static PacketSnapshot ofId(int id) {
        return PacketSnapshot.of(new PacketPlayOutTransaction().setTransactionId(id));
    }
}