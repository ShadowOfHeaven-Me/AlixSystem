package ua.nanit.limbo.protocol.packets.play.payload;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInPluginMessage extends InRetrooperPacket<WrapperPlayClientPluginMessage> {

    public PacketPlayInPluginMessage() {
        super(WrapperPlayClientPluginMessage.class);
    }

    @Override
    public boolean isSkippable(ClientConnection conn) {
        return !conn.getDuplexHandler().passPayloads;
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getIntegration().fireCustomPayloadEvent(conn, this.wrapper().getChannelName(), this.wrapper().getData());
    }
}