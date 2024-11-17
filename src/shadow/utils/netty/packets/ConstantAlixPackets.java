package shadow.utils.netty.packets;

import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import shadow.utils.users.types.AlixUser;

final class ConstantAlixPackets implements AlixPacket {

    private final ByteBuf[] buffers;

    ConstantAlixPackets(PacketWrapper<?>[] wrappers) {
        this.buffers = AlixPacket.createConstByteBufs(wrappers);
    }

    @Override
    public void write(AlixUser user) {
        for (ByteBuf buf : this.buffers) user.silentContext().write(buf.duplicate());
    }

    @Override
    public void writeAndFlush(AlixUser user) {
        for (ByteBuf buf : this.buffers) user.silentContext().writeAndFlush(buf.duplicate());
    }
}