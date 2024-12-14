package alix.common.utils.netty;

import alix.common.utils.other.throwable.AlixException;
import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.*;

public final class BufUtils {

    public static final ByteBufAllocator POOLED = PooledByteBufAllocator.DEFAULT;
    public static final ByteBufAllocator UNPOOLED = UnpooledByteBufAllocator.DEFAULT; //new UnpooledByteBufAllocator(true, true);//remember to never explicitly enable 'no cleaner'
    //private static final boolean NO_CLEANER = PlatformDependent.hasDirectBufferNoCleanerConstructor();

/*    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    }*/

    /*public static ByteBuf buffer() {
        return unpooledBuffer(); //ALLOC.ioBuffer();
    }*/

    public static ByteBuf unpooledBuffer() {
        return UNPOOLED.directBuffer();
    }

    public static ByteBuf unpooledBuffer(int capacity) {
        return UNPOOLED.directBuffer(capacity);
    }

    public static ByteBuf pooledBuffer() {
        return POOLED.directBuffer();
    }

    public static ByteBuf pooledBuffer(int capacity) {
        return POOLED.directBuffer(capacity);
    }

    public static ByteBuf createBuffer(PacketWrapper<?> wrapper) {
        return createBuffer(wrapper, true);
    }

    public static ByteBuf createBuffer(PacketWrapper<?> wrapper, boolean unpooled) {
        return createBuffer0(wrapper, unpooled ? unpooledBuffer() : pooledBuffer());
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
        return createBufferNoID(wrapper, unpooledBuffer());
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

    public static ByteBuf constBuffer(PacketWrapper<?> wrapper, boolean unpooled) {
        return constBuffer(createBuffer(wrapper, unpooled));
    }

    public static ByteBuf constBuffer(ByteBuf dynamicBuf) {//Unreleasable(ReadOnly(ByteBuf)))
        return Unpooled.unreleasableBuffer(dynamicBuf.asReadOnly());
    }
}