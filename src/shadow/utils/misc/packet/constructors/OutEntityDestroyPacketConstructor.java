package shadow.utils.misc.packet.constructors;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;

public final class OutEntityDestroyPacketConstructor {

    public static ByteBuf constructSingleDynamic(int entityId) {
        return NettyUtils.createBuffer(new WrapperPlayServerDestroyEntities(entityId));
    }

    public static ByteBuf constructDynamic(int startIdRangeSelfIncluded, int entityCount) {
        int[] ids = new int[entityCount];
        for (int i = 0; i < entityCount; i++) {
            ids[i] = startIdRangeSelfIncluded + i;
        }

        return NettyUtils.createBuffer(new WrapperPlayServerDestroyEntities(ids));
    }
}