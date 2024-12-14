package nanolimbo.alix.connection.captcha;

import nanolimbo.alix.protocol.PacketSnapshot;
import nanolimbo.alix.protocol.packets.play.transaction.PacketPlayOutTransaction;

public final class Transactions {


    private static PacketSnapshot ofId(int id) {
        return PacketSnapshot.of(new PacketPlayOutTransaction().setId(id));
    }

}