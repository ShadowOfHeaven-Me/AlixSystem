package ua.nanit.limbo.protocol.packets.play.move;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInPosition extends InRetrooperPacket<WrapperPlayClientPlayerPosition> implements FlyingPacket {

    public PacketPlayInPosition() {
        super(WrapperPlayClientPlayerPosition.class);
        FlyingReadUtils.setPositionChanged(this.wrapper());
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}