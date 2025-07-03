package shadow.utils.netty.opt;

import io.netty.buffer.ByteBuf;

public interface PacketFactory {

    ByteBuf prepareConstToSend(ByteBuf constByteBuf);

    static PacketFactory of(boolean writeConst) {
        return writeConst ? new ConstPacketFactory() : new DynamicPacketFactory();
    }
}