package ua.nanit.limbo.connection.pipeline.compression;

import alix.common.utils.collections.queue.AlixQueue;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
import alix.common.utils.netty.BufUtils;
import alix.common.utils.netty.FastNettyUtils;
import alix.common.utils.netty.safety.NettySafety;
import alix.common.utils.other.throwable.AlixError;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

final class CompressionHandlerImpl implements CompressionHandler {

    private static final AlixQueue<CompressionHandlerImpl> handlers = new ConcurrentAlixDeque<>();
    //Why the hell is a normal Thread used instead of FastThreadLocalThread in ServerConnection?
    //The whole fuckin netty performance would increase... bruh
    private static final ThreadLocal<CompressionHandlerImpl> CACHE = new ThreadLocal<>();
    private final ByteBufAllocator alloc;
    private final CompressionImpl impl;
    //private final CompressionSupplier selfSupplier;

    private CompressionHandlerImpl(ByteBufAllocator alloc, CompressionLevel level) {
        this.alloc = alloc;
        this.impl = CompressUtil.newImpl(alloc, level);

        //this.selfSupplier = CompressionSupplier.supply0(this);
        handlers.offerLast(this);
        //channel.closeFuture().addListener(f -> this.impl.release0());
    }

    static void releaseAll() {
        handlers.forEach(c -> c.impl.release0());
        handlers.clear();
    }

    /*private static void assertFastThreadLocalThread() {
        if (!(Thread.currentThread() instanceof FastThreadLocalThread))
            throw new AlixException("Thread " + Thread.currentThread() + " is not a FastThreadLocalThread!");
    }*/

    //Netty Threads do not have any special assignment (for whatever reason)
    /*@ScheduledForFix
    private static void assertNettyThread() {
        if (!Thread.currentThread().getName().startsWith("Netty Server IO"))//this name is platform-dependent
            throw new AlixException("Thread " + Thread.currentThread() + " is not a Netty thread!");
    }*/

    static CompressionHandlerImpl getHandler0(Channel channel) {
        //Must be invoked in the netty eventLoop
        if (!channel.eventLoop().inEventLoop()) throw new AlixError("Not in eventLoop");

        CompressionHandlerImpl cache = CACHE.get();
        if (cache != null) return cache;

        //use the pooled buf alloc for dynamic compression ops
        CompressionHandlerImpl newHandler = new CompressionHandlerImpl(BufUtils.POOLED, CompressionLevel.DEFAULT);//default compression
        CACHE.set(newHandler);
        return newHandler;
    }

    //use the unpooled buf alloc for const compression ops
    static CompressionHandlerImpl createUnpooledUncached0(CompressionLevel level) {
        return new CompressionHandlerImpl(BufUtils.UNPOOLED, level);
    }

    //https://wiki.vg/Protocol#With_compression
    //21 bits for 3x 7 encoding bits, and 2 continuation bits = 23 bits
    //if (readableBytes > 1 << 21) Log.warning("Compressing a packet larger than 2^21 bytes");

    @Override
    public ByteBuf compress(ByteBuf in) throws Exception {
        int readableBytes = in.readableBytes();

        if (readableBytes < COMPRESSION_THRESHOLD) {
            ByteBuf out = this.alloc.directBuffer(readableBytes + 1);
            out.writeByte(0);//no compression
            out.writeBytes(in);

            in.release();

            return out;
        }

        return this.impl.compress0(in);
    }

    @Override
    public ByteBuf decompress(ByteBuf in) throws Exception {
        if (!in.isReadable()) return null;

        int length = FastNettyUtils.readVarInt(in);
        if (length == 0) return in;//uncompressed

        NettySafety.validateUserInputBufAlloc(length);
        return this.impl.decompress0(in, length);
    }

    /*@Override
    public CompressionSupplier selfSupplier() {
        return this.selfSupplier;
    }*/
}