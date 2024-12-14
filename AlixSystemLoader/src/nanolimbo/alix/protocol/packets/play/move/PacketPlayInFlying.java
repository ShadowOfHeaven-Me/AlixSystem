package nanolimbo.alix.protocol.packets.play.move;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.retrooper.InRetrooperPacket;
import nanolimbo.alix.server.LimboServer;

public final class PacketPlayInFlying extends InRetrooperPacket<WrapperPlayClientPlayerFlying> implements FlyingPacket {

    public PacketPlayInFlying() {
        super(WrapperPlayClientPlayerFlying.class);
        //I think this is correct?
        FlyingReadUtils.setPositionChanged(this.wrapper());
        FlyingReadUtils.setRotationChanged(this.wrapper());
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}