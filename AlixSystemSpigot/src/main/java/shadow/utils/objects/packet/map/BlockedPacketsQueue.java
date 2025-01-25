package shadow.utils.objects.packet.map;

import alix.common.utils.collections.queue.network.AlixNetworkDeque;
import alix.common.utils.other.annotation.OptimizationCandidate;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;
import alix.common.utils.netty.FastNettyUtils;
import shadow.utils.users.types.UnverifiedUser;

public final class BlockedPacketsQueue {

    //private static final int size = AlixPacketType.values().length;

    //I mean, there's gotta be a better way than just resending all of these packets
    @OptimizationCandidate
    private final AlixNetworkDeque<ByteBuf> dynamicPackets;
    //private int size;
    //private final ByteBuf[] overridingPackets;

    public BlockedPacketsQueue() {
        this.dynamicPackets = new AlixNetworkDeque<>(10);
        //this.overridingPackets = new ByteBuf[size];
    }

    public void addDynamic(ProtocolPacketEvent event) {
        int packetId = event.getPacketId();
        byte varIntBytes = FastNettyUtils.countBytesToEncodeVarInt(packetId); //count the VarInt's bytes
        //4 - (Integer.numberOfLeadingZeros(packetId) >> 3);//count the int's bytes

        ByteBuf eventBuf = (ByteBuf) event.getByteBuf();
        ByteBuf buf = NettyUtils.directPooledBuffer(eventBuf.capacity() + varIntBytes);

        //prefix the ByteBuf with the packet id
        FastNettyUtils.writeVarInt(buf, packetId, varIntBytes);//reuse the known VarInt's size
        buf.writeBytes(eventBuf);

        this.dynamicPackets.offerLast(buf);
        //Main.debug("SIZE: " + (++this.size));
    }

/*    public void putOverriding(AlixPacketType type, ProtocolPacketEvent<?> event) {
        ByteBuf overriden = this.overridingPackets[type.ordinal()];
        if (overriden != null) overriden.release();

        ByteBuf buf = (ByteBuf) event.getByteBuf();
        this.overridingPackets[type.ordinal()] = buf.copy();
    }*/

    public void writeTo(UnverifiedUser user) {
        this.dynamicPackets.forEach(user::writeSilently);
        //for (ByteBuf buf : this.overridingPackets) if (buf != null) user.writeSilently(buf);
    }

    public void release() {
        this.dynamicPackets.forEach(ByteBuf::release);
        //for (ByteBuf buf : this.overridingPackets) if (buf != null) buf.release();
    }
}