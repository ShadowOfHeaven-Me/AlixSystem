package ua.nanit.limbo.protocol.packets.play.move;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInRotation extends InRetrooperPacket<WrapperPlayClientPlayerRotation> implements FlyingPacket {

    public PacketPlayInRotation() {
        super(WrapperPlayClientPlayerRotation.class);
        FlyingReadUtils.setRotationChanged(this.wrapper());
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}