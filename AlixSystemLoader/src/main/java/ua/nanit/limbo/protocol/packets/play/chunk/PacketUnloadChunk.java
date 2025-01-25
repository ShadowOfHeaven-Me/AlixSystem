package ua.nanit.limbo.protocol.packets.play.chunk;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketUnloadChunk extends OutRetrooperPacket<WrapperPlayServerUnloadChunk> {

    public PacketUnloadChunk() {
        super(WrapperPlayServerUnloadChunk.class);
    }

    public PacketUnloadChunk setX(int x) {
        this.wrapper().setChunkX(x);
        return this;
    }

    public PacketUnloadChunk setZ(int z) {
        this.wrapper().setChunkX(z);
        return this;
    }
}
