package alix.common.utils.netty;

import alix.common.utils.other.throwable.AlixException;
import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.*;

public final class BufUtils {

    //have the bufs managed by the GC (for now disabled)
    //TODO: Add a PooledByteBufAllocator option for dynamic bufs
    public static final ByteBufAllocator POOLED = PooledByteBufAllocator.DEFAULT;
    public static final ByteBufAllocator UNPOOLED = new UnpooledByteBufAllocator(true, true);//remember to never explicitly enable 'no cleaner'
    //private static final boolean NO_CLEANER = PlatformDependent.hasDirectBufferNoCleanerConstructor();

/*    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    }*/

    public static ByteBuf buffer() {
        return directUnpooledBuffer(); //ALLOC.ioBuffer();
    }

    public static ByteBuf directUnpooledBuffer() {
        return UNPOOLED.directBuffer();
    }

    public static ByteBuf directPooledBuffer(int capacity) {
        return POOLED.directBuffer(capacity);
    }

    public static ByteBuf createBuffer(PacketWrapper<?> wrapper) {
        return createBuffer(wrapper, true);
    }

    //a lotta sense with the "direct ? directBuffer() : buffer()" xd
    public static ByteBuf createBuffer(PacketWrapper<?> wrapper, boolean direct) {
        return createBuffer0(wrapper, direct ? directUnpooledBuffer() : buffer());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ByteBuf createBuffer0(PacketWrapper<?> wrapper, ByteBuf emptyByteBuf) {
        if (wrapper.buffer != null)
            throw new AlixException("Incorrect invocation of BufUtils.createBuffer0 - buffer exists");

        int packetId = wrapper.getPacketTypeData().getNativePacketId();
        if (packetId < 0) throw new AlixException("Failed to create packet " + wrapper.getClass().getSimpleName() + " in server version "
                + PacketEvents.getAPI().getServerManager().getVersion() + "! Contact the developer and show him this error! " +
                "Otherwise Alix will not work as intended!");

        wrapper.buffer = emptyByteBuf;
        wrapper.writeVarInt(packetId);
        wrapper.write();

        return emptyByteBuf;//(ByteBuf) wrapper.buffer;
    }

    public static ByteBuf createBufferNoID(PacketWrapper<?> wrapper) {
        return createBufferNoID(wrapper, directUnpooledBuffer());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ByteBuf createBufferNoID(PacketWrapper<?> wrapper, ByteBuf emptyByteBuf) {
        if (wrapper.buffer != null)
            throw new AlixException("Incorrect invocation of NettyUtils.createBufferNoID - buffer exists");

        wrapper.buffer = emptyByteBuf;
        wrapper.write();

        return emptyByteBuf;//(ByteBuf) wrapper.buffer;
    }

    public static ByteBuf constBuffer(PacketWrapper<?> wrapper) {
        return constBuffer(wrapper, true);
    }

    public static ByteBuf constBuffer(PacketWrapper<?> wrapper, boolean direct) {
        return constBuffer(createBuffer(wrapper, direct));
    }

    public static ByteBuf constBuffer(ByteBuf dynamicBuf) {//Unreleasable(ReadOnly(ByteBuf)))
        return Unpooled.unreleasableBuffer(dynamicBuf.asReadOnly());
    }
}