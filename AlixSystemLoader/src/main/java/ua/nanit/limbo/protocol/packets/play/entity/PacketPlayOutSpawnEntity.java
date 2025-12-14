package ua.nanit.limbo.protocol.packets.play.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

import java.util.Optional;
import java.util.UUID;

public final class PacketPlayOutSpawnEntity extends OutRetrooperPacket<WrapperPlayServerSpawnEntity> {

    public PacketPlayOutSpawnEntity() {
        super(WrapperPlayServerSpawnEntity.class);
    }

    public PacketPlayOutSpawnEntity(WrapperPlayServerSpawnEntity wrapper) {
        super(wrapper);
    }

    public static PacketPlayOutSpawnEntity of(int entityId, UUID uuid, EntityType type, Vector3d pos) {
        //Optional.of(UUID.randomUUID())?
        return new PacketPlayOutSpawnEntity(new WrapperPlayServerSpawnEntity(entityId, Optional.of(uuid),
                type, pos, 0, 0, 0, 0, Optional.empty()));
    }
}