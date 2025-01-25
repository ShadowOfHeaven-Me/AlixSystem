package ua.nanit.limbo.protocol.packets.play.animation;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInAnimation extends InRetrooperPacket<WrapperPlayClientAnimation> {

    public PacketPlayInAnimation() {
        super(WrapperPlayClientAnimation.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }
}