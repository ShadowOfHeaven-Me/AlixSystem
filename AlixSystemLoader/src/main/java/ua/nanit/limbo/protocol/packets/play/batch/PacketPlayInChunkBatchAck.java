package ua.nanit.limbo.protocol.packets.play.batch;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChunkBatchAck;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInChunkBatchAck extends InRetrooperPacket<WrapperPlayClientChunkBatchAck> {

    public PacketPlayInChunkBatchAck() {
        super(WrapperPlayClientChunkBatchAck.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }
}