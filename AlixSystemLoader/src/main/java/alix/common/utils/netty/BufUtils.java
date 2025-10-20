package alix.common.utils.netty;

import alix.common.utils.AlixCommonHandler;
import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.*;

import java.util.List;

public final class BufUtils {

    public static final ByteBufAllocator POOLED = PooledByteBufAllocator.DEFAULT;
    public static final ByteBufAllocator UNPOOLED = UnpooledByteBufAllocator.DEFAULT; //new UnpooledByteBufAllocator(true, true);//remember to never explicitly enable 'no cleaner'
    private static final boolean PREFER_DIRECT = AlixCommonHandler.preferDirectBufs();
    //private static final boolean NO_CLEANER = PlatformDependent.hasDirectBufferNoCleanerConstructor();

/*    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    }*/

    /*public static ByteBuf buffer() {
        return unpooledBuffer(); //ALLOC.ioBuffer();
    }*/

    private static ByteBuf buffer0(ByteBufAllocator alloc) {
        return PREFER_DIRECT ? alloc.directBuffer() : alloc.heapBuffer();
    }

    private static ByteBuf buffer0(ByteBufAllocator alloc, int capacity) {
        return PREFER_DIRECT ? alloc.directBuffer(capacity) : alloc.heapBuffer(capacity);
    }

    public static ByteBuf unpooledBuffer() {
        return buffer0(UNPOOLED);
    }

    public static ByteBuf unpooledBuffer(int capacity) {
        return buffer0(UNPOOLED, capacity);
    }

    public static ByteBuf pooledBuffer() {
        return buffer0(POOLED);
    }

    public static ByteBuf pooledBuffer(int capacity) {
        return buffer0(POOLED, capacity);
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

    public static ByteBuf constCompositeBuffer(List<ByteBuf> bufs) {//Unreleasable(ReadOnly(ByteBuf)))
        return Unpooled.unreleasableBuffer(Unpooled.unmodifiableBuffer(bufs.toArray(new ByteBuf[0])));
    }
}