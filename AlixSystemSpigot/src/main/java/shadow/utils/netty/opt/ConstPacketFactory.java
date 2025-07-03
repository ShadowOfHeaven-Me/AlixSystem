package shadow.utils.netty.opt;

import io.netty.buffer.ByteBuf;

final class ConstPacketFactory implements PacketFactory {

    @Override
    public ByteBuf prepareConstToSend(ByteBuf constByteBuf) {
        return constByteBuf.duplicate();//we only need to duplicate it (so not copy), since the contents of a constant ByteBuf are unmodifiable as per the constBuffer method
    }
}