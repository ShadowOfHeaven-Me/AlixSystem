package ua.nanit.limbo.protocol.packets.play.batch;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkBatchEnd;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutChunkBatchEnd extends OutRetrooperPacket<WrapperPlayServerChunkBatchEnd> {

    public PacketPlayOutChunkBatchEnd() {
        super(WrapperPlayServerChunkBatchEnd.class);
    }

    /*@Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }*/
}