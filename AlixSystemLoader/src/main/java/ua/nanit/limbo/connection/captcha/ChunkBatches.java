package ua.nanit.limbo.connection.captcha;

import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.batch.PacketPlayOutChunkBatchEnd;

public final class ChunkBatches {

    //public static final PacketSnapshot BATCH_START = PacketSnapshot.of(new PacketPlayOutChunkBatchStart());
    public static final PacketSnapshot BATCH_END = PacketSnapshot.of(new PacketPlayOutChunkBatchEnd());

}