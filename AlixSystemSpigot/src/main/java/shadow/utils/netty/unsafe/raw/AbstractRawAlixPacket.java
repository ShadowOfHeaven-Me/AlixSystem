package shadow.utils.netty.unsafe.raw;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.function.Consumer;

abstract class AbstractRawAlixPacket implements RawAlixPacket {

    final Channel channel;
    private final Consumer<ByteBuf> release;

    AbstractRawAlixPacket(Channel channel, Consumer<ByteBuf> release) {
        this.channel = channel;
        this.release = release;
    }

    @Override
    public final void writeAndFlush() {
        this.write();
        this.channel.unsafe().flush();
    }

    final void release0(ByteBuf buf) {
        this.release.accept(buf);
    }
}