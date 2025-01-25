package ua.nanit.limbo.protocol.packets.play.move;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInPositionAndRotation extends InRetrooperPacket<WrapperPlayClientPlayerPositionAndRotation> implements FlyingPacket {

    public PacketPlayInPositionAndRotation() {
        super(WrapperPlayClientPlayerPositionAndRotation.class);
        FlyingReadUtils.setPositionChanged(this.wrapper());
        FlyingReadUtils.setRotationChanged(this.wrapper());
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}