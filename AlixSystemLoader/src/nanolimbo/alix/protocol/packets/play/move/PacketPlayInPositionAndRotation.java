package nanolimbo.alix.protocol.packets.play.move;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.retrooper.InRetrooperPacket;
import nanolimbo.alix.server.LimboServer;

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