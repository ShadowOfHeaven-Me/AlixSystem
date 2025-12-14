package ua.nanit.limbo.connection.captcha;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.PacketSnapshots;
import ua.nanit.limbo.protocol.packets.play.entity.PacketPlayOutSpawnEntity;

public final class Entities {

    public static final PacketSnapshot SAME_ID = PacketSnapshot.of(PacketPlayOutSpawnEntity.of(
                    PacketSnapshots.PLAYER_ENTITY_ID, PacketSnapshots.PLAYER_UUID,
                    EntityTypes.ITEM_FRAME, Vector3d.zero()
            ));

}