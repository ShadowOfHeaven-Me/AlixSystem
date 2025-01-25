package ua.nanit.limbo.protocol.packets.play.batch;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkBatchBegin;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutChunkBatchStart extends OutRetrooperPacket<WrapperPlayServerChunkBatchBegin> {

    public PacketPlayOutChunkBatchStart() {
        super(WrapperPlayServerChunkBatchBegin.class);
    }

    /*@Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getCaptchaState().handle(this);
    }*/
}