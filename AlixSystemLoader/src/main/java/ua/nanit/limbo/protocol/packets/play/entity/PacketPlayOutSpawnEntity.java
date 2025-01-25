package ua.nanit.limbo.protocol.packets.play.entity;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutSpawnEntity extends OutRetrooperPacket<WrapperPlayServerSpawnEntity> {

    public PacketPlayOutSpawnEntity() {
        super(WrapperPlayServerSpawnEntity.class);
    }

    public PacketPlayOutSpawnEntity(WrapperPlayServerSpawnEntity wrapper) {
        super(wrapper);
    }
}