package shadow.utils.netty.opt;

import io.netty.buffer.ByteBuf;

final class DynamicPacketFactory implements PacketFactory {

    @Override
    public ByteBuf prepareConstToSend(ByteBuf constByteBuf) {
        /*var buf = BufUtils.pooledBuffer(constByteBuf.readableBytes());
        buf.writeBytes(constByteBuf);*/
        //Both, ReadOnly & Unreleasable should unwrap the copy(), but whatever
        return constByteBuf.unwrap().unwrap().copy();
    }
}