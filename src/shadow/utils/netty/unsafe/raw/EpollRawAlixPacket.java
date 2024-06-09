package shadow.utils.netty.unsafe.raw;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

final class EpollRawAlixPacket extends AbstractRawAlixPacket {

    //The minimum is:
    //68 bytes for ipv4 https://networkengineering.stackexchange.com/questions/76459/what-is-the-minimum-mtu-of-ipv4-68-bytes-or-576-bytes
    //1280 bytes for ipv6
    //However, I assume no sane being would set in any lower than 800, so removing the max capacity TCP and IP header(s), we get 760
    private static final int IPV4_SIZE_THRESHOLD = 760, IPV6_SIZE_THRESHOLD = 1280;
    private final ByteBuf[] buffers;

    EpollRawAlixPacket(Channel channel, ByteBuf[] buffers, Function<ByteBuf, ByteBuf> transformer, Consumer<ByteBuf> release) {
        super(channel, release);
        this.buffers = getRawBuffers(channel, buffers, transformer);
    }

    private static ByteBuf[] getRawBuffers(Channel channel, ByteBuf[] buffers, Function<ByteBuf, ByteBuf> transformer) {
        List<ByteBuf> raws = new ArrayList<>((buffers.length >> 4) + 10);//roughly guess the size
        int threshold = ((InetSocketAddress) channel.remoteAddress()).getAddress().getClass() == Inet4Address.class ? IPV4_SIZE_THRESHOLD : IPV6_SIZE_THRESHOLD;

        int currentSize = 0;
        CompositeByteBuf currentCompositeByteBuf = Unpooled.compositeBuffer();

        for (ByteBuf buf : buffers) {
            if (currentSize + buf.readableBytes() < threshold) {
                currentSize += buf.readableBytes();
                currentCompositeByteBuf.addComponent(true, buf);//remember to increase the writer index when adding the ByteBuf components
            } else {
                currentSize = 0;
                raws.add(transformer.apply(currentCompositeByteBuf));
                currentCompositeByteBuf = Unpooled.compositeBuffer();
            }
        }
        return raws.toArray(new ByteBuf[0]);
    }

    @Override
    public void write() {
        for (ByteBuf buf : this.buffers) RawAlixPacket.writeRaw(buf, this.channel);
    }

    @Override
    public void release() {
        for (ByteBuf buf : this.buffers) this.release0(buf);
    }
}