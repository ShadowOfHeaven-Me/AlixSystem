package shadow.utils.netty.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.AlixUser;

public interface AlixPacket {

    void write(AlixUser user);

    void writeAndFlush(AlixUser user);

    static AlixPacket constant(PacketWrapper<?>... packetWrappers) {
        return new ConstantAlixPackets(packetWrappers);
    }

    static AlixPacket dynamic(PacketWrapper<?>... packetWrappers) {
        return new DynamicAlixPackets(packetWrappers);
    }

    static ByteBuf[] createConstByteBufs(PacketWrapper<?>[] wrappers) {
        ByteBuf[] buffers = new ByteBuf[wrappers.length];
        for (int i = 0; i < wrappers.length; i++) buffers[i] = NettyUtils.constBuffer(wrappers[i]);
        return buffers;
    }

    static ByteBuf[] createDynamicByteBufs(PacketWrapper<?>... wrappers) {
        ByteBuf[] buffers = new ByteBuf[wrappers.length];
        for (int i = 0; i < wrappers.length; i++) buffers[i] = NettyUtils.createBuffer(wrappers[i]);
        return buffers;
    }
}