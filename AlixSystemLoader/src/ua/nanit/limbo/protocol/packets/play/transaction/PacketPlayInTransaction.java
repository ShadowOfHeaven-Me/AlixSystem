package ua.nanit.limbo.protocol.packets.play.transaction;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;

public final class PacketPlayInTransaction extends InRetrooperPacket<WrapperPlayClientWindowConfirmation> {

    public PacketPlayInTransaction() {
        super(WrapperPlayClientWindowConfirmation.class);
    }

    /*@Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }*/
}