package ua.nanit.limbo.protocol.packets.play.rename;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInItemRename extends InRetrooperPacket<WrapperPlayClientNameItem> {

    public PacketPlayInItemRename() {
        super(WrapperPlayClientNameItem.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}