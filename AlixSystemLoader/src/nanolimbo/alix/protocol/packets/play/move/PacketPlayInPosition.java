package nanolimbo.alix.protocol.packets.play.move;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.retrooper.InRetrooperPacket;
import nanolimbo.alix.server.LimboServer;

public final class PacketPlayInPosition extends InRetrooperPacket<WrapperPlayClientPlayerPosition> implements FlyingPacket {

    public PacketPlayInPosition() {
        super(WrapperPlayClientPlayerPosition.class);
        FlyingReadUtils.setPositionChanged(this.wrapper());
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}