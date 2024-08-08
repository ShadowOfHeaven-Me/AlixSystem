package shadow.utils.objects.packet.map;

import alix.common.utils.collections.queue.AlixDeque;
import alix.common.utils.other.annotation.OptimizationCandidate;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import io.netty.buffer.ByteBuf;
import shadow.utils.users.types.UnverifiedUser;

public final class BlockedPacketsMap {

    //private static final int size = AlixPacketType.values().length;

    //I mean, there's gotta be a better way than just resending all of these packets
    @OptimizationCandidate
    private final AlixDeque<ByteBuf> dynamicPackets;
    //private final ByteBuf[] overridingPackets;

    public BlockedPacketsMap() {
        this.dynamicPackets = new AlixDeque<>();
        //this.overridingPackets = new ByteBuf[size];
    }

    public void addDynamic(ProtocolPacketEvent<?> event) {
        ByteBuf buf = (ByteBuf) event.getByteBuf();
        this.dynamicPackets.offerLast(buf.copy());
    }

/*    public void putOverriding(AlixPacketType type, ProtocolPacketEvent<?> event) {
        ByteBuf overriden = this.overridingPackets[type.ordinal()];
        if (overriden != null) overriden.release();

        ByteBuf buf = (ByteBuf) event.getByteBuf();
        this.overridingPackets[type.ordinal()] = buf.copy();
    }*/

    public void sendTo(UnverifiedUser user) {
        this.dynamicPackets.forEach(user::writeSilently);
        //for (ByteBuf buf : this.overridingPackets) if (buf != null) user.writeSilently(buf);
    }

    public void release() {
        this.dynamicPackets.forEach(ByteBuf::release);
        //for (ByteBuf buf : this.overridingPackets) if (buf != null) buf.release();
    }
}