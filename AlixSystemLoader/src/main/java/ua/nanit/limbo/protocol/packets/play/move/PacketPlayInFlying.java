package ua.nanit.limbo.protocol.packets.play.move;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInFlying extends InRetrooperPacket<WrapperPlayClientPlayerFlying> implements FlyingPacket {

    public PacketPlayInFlying() {
        super(WrapperPlayClientPlayerFlying.class);
        //I think this is correct?
        FlyingReadUtils.setPositionChanged(this.wrapper());
        FlyingReadUtils.setRotationChanged(this.wrapper());
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}