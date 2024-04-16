package shadow.utils.holders.packet.constructors;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;

public final class OutHeldItemSlotPacketConstructor {

    //public static final Object SLOT_0 = construct(0);
/*    private static final Constructor<?> constructor;

    static {
        Class<?> packetClazz = ReflectionUtils.outHeldItemSlotPacketClass;
        constructor = ReflectionUtils.getConstructor(packetClazz, int.class);
    }*/

    public static ByteBuf construct(int slot) {
        try {
            return NettyUtils.constBuffer(new WrapperPlayServerHeldItemChange(slot));
            //return constructor.newInstance(slot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}