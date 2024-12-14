package nanolimbo.alix.protocol.packets.play.transaction;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutTransaction extends OutRetrooperPacket<WrapperPlayServerWindowConfirmation> {

    public PacketPlayOutTransaction() {
        super(WrapperPlayServerWindowConfirmation.class);
    }

    public PacketOut setId(int id) {
        this.wrapper().setWindowId(id);
        return this;
    }
}