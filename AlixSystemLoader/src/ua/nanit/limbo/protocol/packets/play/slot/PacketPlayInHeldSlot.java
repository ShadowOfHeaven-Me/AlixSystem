package ua.nanit.limbo.protocol.packets.play.slot;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInHeldSlot extends InRetrooperPacket<WrapperPlayClientHeldItemChange> {

    public PacketPlayInHeldSlot() {
        super(WrapperPlayClientHeldItemChange.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}