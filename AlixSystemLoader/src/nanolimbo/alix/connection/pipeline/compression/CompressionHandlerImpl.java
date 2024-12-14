package nanolimbo.alix.connection.pipeline.compression;

import alix.common.utils.collections.queue.AlixQueue;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
import alix.common.utils.netty.BufUtils;
import alix.common.utils.netty.FastNettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import nanolimbo.alix.server.Log;

final class CompressionHandlerImpl implements CompressionHandler {

    private static final AlixQueue<CompressionHandlerImpl> handlers = new ConcurrentAlixDeque<>();
    //Why the hell is a normal Thread used instead of FastThreadLocalThread in ServerConnection?
    //The whole fuckin netty performance would increase... bruh
    private static final ThreadLocal<CompressionHandlerImpl> CACHE = new ThreadLocal<>();
    private final ByteBufAllocator alloc;
    private final CompressionImpl impl;

    private CompressionHandlerImpl(ByteBufAllocator alloc) {
        this.alloc = alloc;
        this.impl = CompressUtil.newImpl(alloc);
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

    //Must be invoked in the netty eventLoop
    static CompressionHandlerImpl getHandler0() {
        CompressionHandlerImpl cache = CACHE.get();
        if (cache != null) return cache;

        //use the pooled buf alloc for dynamic compression ops
        CompressionHandlerImpl newHandler = new CompressionHandlerImpl(BufUtils.POOLED);
        CACHE.set(newHandler);
        return newHandler;
    }

    //use the unpooled buf alloc for const compression ops
    static CompressionHandlerImpl createUnpooledUncached0() {
        return new CompressionHandlerImpl(BufUtils.UNPOOLED);
    }

    @Override
    public ByteBuf compress(ByteBuf in) throws Exception {
        int readableBytes = in.readableBytes();
        //https://wiki.vg/Protocol#With_compression
        //21 bits for 3x 7 encoding bits, and 2 continuation bits = 23 bits
        if (readableBytes > 1 << 21) Log.warning("Compressing a packet larger than 2^21 bytes");

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

        return this.impl.decompress0(in, length);
    }
}