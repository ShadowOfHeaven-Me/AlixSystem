package shadow.utils.netty.unsafe.raw;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.function.Consumer;
import java.util.function.Function;

final class NIORawAlixPacket extends AbstractRawAlixPacket {

    //Nio transport allows for a single byte buf
    private final ByteBuf buf;

    NIORawAlixPacket(Channel channel, ByteBuf[] buffers, Function<ByteBuf, ByteBuf> transformer, Consumer<ByteBuf> release) {
        super(channel, release);
        this.buf = transformer.apply(Unpooled.compositeBuffer().addComponents(true, buffers));//remember to increase the writer index when adding the ByteBuf components
        //RawAlixPacket.checkLengthValid(this.buf);
    }

    @Override
    public void write() {
        RawAlixPacket.writeRaw(this.buf, this.channel);
    }

    @Override
    public void release() {
        this.release0(this.buf);
    }
}