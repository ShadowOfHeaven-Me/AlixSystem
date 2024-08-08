package shadow.utils.netty;


import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import shadow.utils.netty.unsafe.raw.RawAlixPacket;

public final class NettyUtils {

/*    private static final AttributeKey<?> STATE_ATTR = AttributeKey.valueOf("protocol");//EnumProtocol

    public static void validate(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        Object protocolDir = ctx.channel().attr(STATE_ATTR).get();
        List<?> mapper = protocolDir == ReflectionUtils.FROM_CLIENT_DURING_HANDSHAKE ? ReflectionUtils.HANDSHAKE_ID_MAPPER : ReflectionUtils.AFTER_HANDSHAKE_ID_MAPPER;
        int packetId = readPacketId(byteBuf);
        Main.logError("MAPPED: " + mapper.get(packetId));
    }*/

    //have the bufs managed by the GC (for now disabled)
    private static final ByteBufAllocator ALLOC = new UnpooledByteBufAllocator(false, true);//remember to never explicitly enable 'no cleaner'
    //private static final boolean NO_CLEANER = PlatformDependent.hasDirectBufferNoCleanerConstructor();

/*    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    }*/

    public static ByteBuf buffer() {
        return ALLOC.buffer();
    }

    public static ByteBuf directBuffer() {
        return ALLOC.directBuffer();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ChannelHandlerContext getSilentContext(Channel channel) {
        return channel.pipeline().context(PacketEvents.ENCODER_NAME);
    }

    private static ByteBuf prepareConstToSend(ByteBuf constByteBuf) {
        return constByteBuf.duplicate();//we only need to duplicate it (so not copy), since the contents of a constant ByteBuf are unmodifiable as per the constBuffer method
    }

    public static void writeConst(ChannelHandlerContext context, ByteBuf constByteBuf) {
        //AlixUser.DEBUG_TIME();
        //ByteBuf send = context.channel().alloc().buffer().writeBytes(constByteBuf.copy());
        context.write(prepareConstToSend(constByteBuf));
    }

    public static ChannelFuture writeAndFlushConst(ChannelHandlerContext context, ByteBuf constByteBuf) {
        //AlixUser.DEBUG_TIME();
        //ByteBuf send = context.channel().alloc().buffer().writeBytes(constByteBuf.copy());
        return context.writeAndFlush(prepareConstToSend(constByteBuf));
    }

    public static ChannelPromise writeAndFlushConstRaw(Channel channel, ByteBuf constByteBuf) {
        //AlixUser.DEBUG_TIME();
        //ByteBuf send = context.channel().alloc().buffer().writeBytes(constByteBuf.copy());
        ChannelPromise promise = channel.newPromise();
        RawAlixPacket.writeRaw(constByteBuf, channel, promise);
        return promise;
    }

    public static void closeAfterConstSendRaw(Channel channel, ByteBuf constBuf) {
        writeAndFlushConstRaw(channel, constBuf).addListener(ChannelFutureListener.CLOSE);
    }

    public static void closeAfterConstSend(Channel channel, ByteBuf constBuf) {
        writeAndFlushConst(getSilentContext(channel), constBuf).addListener(ChannelFutureListener.CLOSE);
    }

/*    public static ByteBuf newBuffer(int initialCapacity) {
        return AlixUnsafe.hasUnsafe() ? Unpooled.directBuffer(initialCapacity) : Unpooled.buffer(initialCapacity);
    }*/

    //ByteBufAllocator.DEFAULT.buffer();

    public static void writeDynamicWrapper(PacketWrapper<?> wrapper, ChannelHandlerContext context) {
        //AlixUser.DEBUG_TIME();
        context.write(dynamic(wrapper, context));
    }

    public static void writeAndFlushDynamicWrapper(PacketWrapper<?> wrapper, ChannelHandlerContext context) {
        //AlixUser.DEBUG_TIME();
        context.writeAndFlush(dynamic(wrapper, context));
    }

    public static ByteBuf dynamic(PacketWrapper<?> wrapper, ChannelHandlerContext context) {
        return createBuffer0(wrapper, context.alloc().buffer());
    }

    public static ByteBuf createBuffer(PacketWrapper<?> wrapper) {
        return createBuffer(wrapper, false);
    }

    public static ByteBuf createBuffer(PacketWrapper<?> wrapper, boolean direct) {
        return createBuffer0(wrapper, direct ? directBuffer() : buffer());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static ByteBuf createBuffer0(PacketWrapper<?> wrapper, ByteBuf emptyByteBuf) {
        if (wrapper.buffer != null)
            throw new AlixException("Incorrect invocation of NettyUtils.createBuffer - buffer exists");

        wrapper.buffer = emptyByteBuf;
        wrapper.writeVarInt(wrapper.getPacketTypeData().getNativePacketId());
        wrapper.write();

        return emptyByteBuf;//(ByteBuf) wrapper.buffer;
    }

    public static ByteBuf constBuffer(PacketWrapper<?> wrapper) {
        return constBuffer(wrapper, false);
    }

    public static ByteBuf constBuffer(PacketWrapper<?> wrapper, boolean direct) {
        return constBuffer(createBuffer(wrapper, direct));
    }

    public static ByteBuf constBuffer(ByteBuf dynamicBuf) {//Unreleasable(ReadOnly(ByteBuf)))
        return Unpooled.unreleasableBuffer(dynamicBuf.asReadOnly());
    }

    //From PacketDataSerializer#j
/*    public static int readPacketId(ByteBuf buf) {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = buf.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        buf.resetReaderIndex();
        return i;
    }*/

    //The following methods prevent StacklessClosedChannelException

/*    public static void writeAndFlush(Channel channel, Object msg, ChannelFutureListener listener) {
        channel.eventLoop().execute(() -> {
            if (channel.isOpen()) channel.writeAndFlush(msg).addListener(listener);
        });
    }

    public static void writeAndFlush(ChannelHandlerContext ctx, Object msg) {
        ctx.channel().eventLoop().execute(() -> {
            if (ctx.channel().isOpen()) ctx.writeAndFlush(msg);
        });
    }

    public static void writeAndFlush(Channel channel, Object msg) {
        channel.eventLoop().execute(() -> {
            if (channel.isOpen()) channel.writeAndFlush(msg);
        });
    }

    public static void write(Channel channel, Object msg) {
        channel.eventLoop().execute(() -> {
            if (channel.isOpen()) channel.write(msg);
        });
    }

    public static void flush(Channel channel) {
        channel.eventLoop().execute(() -> {
            if (channel.isOpen()) channel.flush();
        });
    }*/

    private NettyUtils() {
    }
}