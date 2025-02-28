package ua.nanit.limbo.protocol.packets.play.config;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientConfigurationAck;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInReconfigureAck extends InRetrooperPacket<WrapperPlayClientConfigurationAck> {

    public PacketPlayInReconfigureAck() {
        super(WrapperPlayClientConfigurationAck.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}