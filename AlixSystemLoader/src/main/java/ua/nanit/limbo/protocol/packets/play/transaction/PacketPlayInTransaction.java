package ua.nanit.limbo.protocol.packets.play.transaction;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInTransaction extends InRetrooperPacket<WrapperPlayClientWindowConfirmation> {

    public PacketPlayInTransaction() {
        super(WrapperPlayClientWindowConfirmation.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}