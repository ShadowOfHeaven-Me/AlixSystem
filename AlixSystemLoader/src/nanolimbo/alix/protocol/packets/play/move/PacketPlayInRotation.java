package nanolimbo.alix.protocol.packets.play.move;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.retrooper.InRetrooperPacket;
import nanolimbo.alix.server.LimboServer;

public final class PacketPlayInRotation extends InRetrooperPacket<WrapperPlayClientPlayerRotation> implements FlyingPacket {

    public PacketPlayInRotation() {
        super(WrapperPlayClientPlayerRotation.class);
        FlyingReadUtils.setRotationChanged(this.wrapper());
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}