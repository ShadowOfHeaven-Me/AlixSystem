package ua.nanit.limbo.protocol.packets.play.transaction;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutTransaction extends OutRetrooperPacket<WrapperPlayServerWindowConfirmation> {

    public PacketPlayOutTransaction() {
        super(WrapperPlayServerWindowConfirmation.class);
    }

    public PacketOut setTransactionId(int id) {
        this.wrapper().setActionId((short) id);
        return this;
    }
}