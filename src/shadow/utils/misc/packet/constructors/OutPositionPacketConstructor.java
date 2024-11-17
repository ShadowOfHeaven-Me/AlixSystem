package shadow.utils.misc.packet.constructors;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import io.netty.buffer.ByteBuf;
import org.bukkit.Location;
import shadow.utils.netty.NettyUtils;

public final class OutPositionPacketConstructor {

    public static ByteBuf constructConst(Location loc) {
        return constructConst(loc, 1);
    }

    public static ByteBuf constructConst(Location loc, int teleportId) {
        return NettyUtils.constBuffer(new WrapperPlayServerPlayerPositionAndLook(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), (byte) 0, teleportId, true));
    }
}