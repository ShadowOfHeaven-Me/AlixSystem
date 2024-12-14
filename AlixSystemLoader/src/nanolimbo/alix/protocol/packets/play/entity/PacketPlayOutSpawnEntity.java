package nanolimbo.alix.protocol.packets.play.entity;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import nanolimbo.alix.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutSpawnEntity extends OutRetrooperPacket<WrapperPlayServerSpawnEntity> {

    public PacketPlayOutSpawnEntity() {
        super(WrapperPlayServerSpawnEntity.class);
    }

    public PacketPlayOutSpawnEntity(WrapperPlayServerSpawnEntity wrapper) {
        super(wrapper);
    }
}