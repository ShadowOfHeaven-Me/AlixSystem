package ua.nanit.limbo.protocol.packets.play;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPosition;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

import java.util.Locale;

public class PacketSpawnPosition extends OutRetrooperPacket<WrapperPlayServerSpawnPosition> {

    public PacketSpawnPosition() {
        super(WrapperPlayServerSpawnPosition.class);
    }

    public PacketSpawnPosition(int x, int y, int z) {
        this();
        this.wrapper().setPosition(new Vector3i(x, y, z));

        var dimension = NanoLimbo.LIMBO.getConfig().getDimensionType().toLowerCase(Locale.ROOT);
        this.wrapper().setDimension(new ResourceLocation("minecraft:" + dimension));
    }
}
