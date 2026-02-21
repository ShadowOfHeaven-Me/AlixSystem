package shadow.utils.netty.packets;

import alix.common.utils.netty.WrapperTransformer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import shadow.utils.users.types.AlixUser;

import java.util.function.Consumer;

public interface AlixPacket {

    void write(AlixUser user);

    default void writeAndFlush(AlixUser user) {
        this.write(user);
        user.flush();
    }

    boolean isConst();

    boolean tryRelease();

    void release0();

    void forEach(Consumer<ByteBuf> consumer);

    //void forEach(Function<ByteBuf, ?> consumer);

    static AlixPacket const0(PacketWrapper<?>... wrappers) {
        return new ConstAlixPacket(wrappers);
    }

    static AlixPacket const0(ByteBuf... bufs) {
        return new ConstAlixPacket(bufs);
    }

    /*static AlixPacket constRawFromConst(ByteBuf... bufs) {
        return new ConstRawAlixPacket(bufs);
    }*/
    /*static AlixPacket constant(PacketWrapper<?>... packetWrappers) {
        return new ConstantAlixPackets(packetWrappers);
    }

    static AlixPacket dynamic(PacketWrapper<?>... packetWrappers) {
        return new DynamicAlixPackets(packetWrappers);
    }*/

    static ByteBuf[] constructBufs(PacketWrapper<?>[] wrappers, WrapperTransformer transformer) {
        ByteBuf[] buffers = new ByteBuf[wrappers.length];
        for (int i = 0; i < wrappers.length; i++) buffers[i] = transformer.apply(wrappers[i]);
        return buffers;
    }
}