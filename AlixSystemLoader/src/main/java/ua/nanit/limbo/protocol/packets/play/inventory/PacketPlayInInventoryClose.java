package ua.nanit.limbo.protocol.packets.play.inventory;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInInventoryClose extends InRetrooperPacket<WrapperPlayClientCloseWindow> {

    public PacketPlayInInventoryClose() {
        super(WrapperPlayClientCloseWindow.class);
    }

    public PacketPlayInInventoryClose(WrapperPlayClientCloseWindow wrapper) {
        super(wrapper);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}