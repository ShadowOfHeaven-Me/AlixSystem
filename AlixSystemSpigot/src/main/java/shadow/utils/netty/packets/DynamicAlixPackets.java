/*
package shadow.utils.netty.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import shadow.utils.users.types.AlixUser;

final class DynamicAlixPackets implements AlixPacket {

    private final ByteBuf[] buffers;

    DynamicAlixPackets(PacketWrapper<?>[] wrappers) {
        this.buffers = AlixPacket.createDynamicByteBufs(wrappers);
    }

    @Override
    public void write(AlixUser user) {
        for (ByteBuf buf : this.buffers) user.silentContext().write(buf);
    }

    @Override
    public void writeAndFlush(AlixUser user) {
        if (this.buffers.length == 1) {
            user.silentContext().writeAndFlush(this.buffers[0]);
            return;
        }
        for (ByteBuf buf : this.buffers) user.silentContext().write(buf);
        user.silentContext().flush();
    }
}*/
