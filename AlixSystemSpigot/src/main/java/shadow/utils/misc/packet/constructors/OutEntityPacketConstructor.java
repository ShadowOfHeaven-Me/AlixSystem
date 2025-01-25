package shadow.utils.misc.packet.constructors;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import org.bukkit.Location;
import shadow.utils.netty.NettyUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public final class OutEntityPacketConstructor {

    public static ByteBuf constData(int entityId, EntityData... data) {
        return NettyUtils.constBuffer(new WrapperPlayServerEntityMetadata(entityId, Arrays.asList(data)));
    }

    public static ByteBuf constSpawn(int entityId, EntityType type, Location loc) {
        return constSpawn(entityId, type, loc, 0);
    }

    public static ByteBuf constSpawn(int entityId, EntityType type, Location loc, int data) {
        return NettyUtils.constBuffer(spawnWrapper(entityId, type, loc, data));
    }

    public static PacketWrapper<?> spawnWrapper(int entityId, EntityType type, Location loc, int data) {
        return new WrapperPlayServerSpawnEntity(
                entityId, Optional.of(UUID.randomUUID()), type, SpigotConversionUtil.fromBukkitLocation(loc).getPosition(),
                loc.getPitch(), loc.getYaw(), 180, data,
                Optional.of(Vector3d.zero()));
    }
}