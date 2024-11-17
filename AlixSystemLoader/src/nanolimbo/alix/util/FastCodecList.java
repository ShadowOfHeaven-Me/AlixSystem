package nanolimbo.alix.util;

import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;

public final class FastCodecList {

    private int index;
    private ByteBuf[] data;

    private FastCodecList() {
        this.data = new ByteBuf[16];
    }

    private void grow(int amount) {
        ByteBuf[] copy = new ByteBuf[this.index + amount];//shouldn't really happen
        System.arraycopy(this.data, 0, copy, 0, this.data.length);
        this.data = copy;
    }

    public void add(ByteBuf buf) {
        if (this.index >= data.length) this.grow(8);

        this.data[this.index++] = buf;
    }

    public void drain(Consumer<ByteBuf> consumer) {
        for (int i = 0; i < this.index; i++) {
            consumer.accept(this.data[i]);
            this.data[i] = null;
        }
        this.index = 0;
    }

    public static FastCodecList createNew() {
        return new FastCodecList();
    }
}