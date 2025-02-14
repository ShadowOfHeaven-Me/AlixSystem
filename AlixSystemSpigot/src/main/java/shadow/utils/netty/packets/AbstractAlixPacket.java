package shadow.utils.netty.packets;

import alix.common.utils.netty.BufRelease;
import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;

abstract class AbstractAlixPacket implements AlixPacket {

    final ByteBuf[] bufs;

    AbstractAlixPacket(ByteBuf[] bufs) {
        this.bufs = bufs;
    }

    @Override
    public void forEach(Consumer<ByteBuf> consumer) {
        for (ByteBuf buf : bufs) consumer.accept(buf);
    }

    @Override
    public void release0() {
        this.forEach(BufRelease.of(this.isConst()));
    }

    @Override
    public boolean tryRelease() {
        if (isConst()) return false;
        this.release0();
        return true;
    }
}