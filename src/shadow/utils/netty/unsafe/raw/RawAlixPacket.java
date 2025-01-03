package shadow.utils.netty.unsafe.raw;

import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import shadow.utils.main.AlixHandler;

import java.util.function.Consumer;
import java.util.function.Function;

public interface RawAlixPacket {

    void write();

    void writeAndFlush();

    void release();

    static void writeRaw(ByteBuf rawBuffer, Channel channel) {
        writeRaw(rawBuffer, channel, channel.voidPromise());
    }

    static ChannelPromise writeRaw(ByteBuf rawBuffer, Channel channel, ChannelPromise promise) {
        try {
            ChannelOutboundBuffer buffer = channel.unsafe().outboundBuffer();
            if (buffer != null) buffer.addMessage(rawBuffer, rawBuffer.readableBytes(), promise);
        } catch (Throwable e) {
            AlixCommonUtils.logException(e);
        }
        return promise;
    }

    static void checkLengthValid(ByteBuf buf) {
        if (buf.readableBytes() > 65536) throw new AlixException("ByteBuf is larger than 65,536 bytes!");
    }

    static RawAlixPacket of(Channel channel, ByteBuf[] buffers, Function<ByteBuf, ByteBuf> transformer, Consumer<ByteBuf> release) {
        return AlixHandler.isEpollTransport ? new EpollRawAlixPacket(channel, buffers, transformer, release) : new NIORawAlixPacket(channel, buffers, transformer, release);
    }
}