package shadow.utils.netty.unsafe;

import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.netty.unsafe.fast.FastNettyUtils;

import java.util.function.Function;

public final class UnsafeNettyUtils {

/*    private static final Method newContext, addFirst0, atomicRemoveFromHandlerList;

    static {
        newContext = ReflectionUtils.getMethodByName(DefaultChannelPipeline.class, "newContext");
        addFirst0 = ReflectionUtils.getMethodByName(DefaultChannelPipeline.class, "addFirst0");
        atomicRemoveFromHandlerList = ReflectionUtils.getMethodByName(DefaultChannelPipeline.class, "atomicRemoveFromHandlerList");
    }*/

/*    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")//IntelliJ got a bit confused here
    private static ChannelHandlerContext addFirst0(ChannelPipeline pipeline, String name, ChannelHandler handler) {
        synchronized (pipeline) {
            try {
                ChannelHandlerContext ctx = (ChannelHandlerContext) newContext.invoke(pipeline, null, name, handler);
                addFirst0.invoke(pipeline, ctx);
                return ctx;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void removeFirst0(ChannelHandlerContext ctx) {
        synchronized (ctx.pipeline()) {
            try {
                atomicRemoveFromHandlerList.invoke(ctx.pipeline(), ctx);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }*/

    public static void sendAndSetRaw(ChannelHandlerContext silentContext, ByteBufHarvester harvester, Function<ByteBuf, ByteBuf> outputTransformer, ByteBuf[] buffers) {
        Channel channel = silentContext.channel();
        if (!channel.eventLoop().inEventLoop()) throw new AlixException("Not in an eventLoop");

        harvester.harvest = true;

        for (int i = 0; i < buffers.length; i++) {
            silentContext.write(buffers[i]);
            if (harvester.harvested == null) throw new AlixException("setRaw - no output from harvester! - " + silentContext.pipeline().names());
            buffers[i] = outputTransformer.apply(harvester.harvested);
            harvester.harvested = null;
        }

        harvester.harvest = false;
        harvester.harvested = null;
        //channel.unsafe().outboundBuffer().addMessage(byteBuf0, byteBuf0.readableBytes(), channel.voidPromise());
    }

    public static ByteBuf sendAndGetRaw(ChannelHandlerContext silentContext, ByteBufHarvester harvester, Function<ByteBuf, ByteBuf> outputTransformer, ByteBuf buffer) {
        Channel channel = silentContext.channel();
        if (!channel.eventLoop().inEventLoop()) throw new AlixException("Not in an eventLoop");

        harvester.harvest = true;

        silentContext.write(buffer);
        ByteBuf harvested = harvester.harvested;

        if (harvested == null)
            throw new AlixException("getRaw - no output from harvester! - " + silentContext.pipeline().names());

        harvester.harvest = false;
        harvester.harvested = null;

        return outputTransformer.apply(harvested);
    }

    public static void fastUnsafeRemove(Channel channel, String handlerName) {
        FastNettyUtils.remove(channel, handlerName);
    }

    /*private static final int COMPRESSION_THRESHOLD;

    static {
        String[] path = AlixUtils.split(Main.plugin.getDataFolder().getAbsolutePath(), File.separatorChar);
        File props = new File(path[path.length - 3] + File.separator + "server.properties");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(props);
        COMPRESSION_THRESHOLD = config.getInt("network-compression-threshold", 256);
    }

    public static ByteBuf getRawFor(ByteBuf buf, AlixUser user) {
        return prepend(compress(transformFor(buf, user)));
    }

    private static int a(int i) {
        for (int j = 1; j < 5; ++j) {
            if ((i & -1 << j * 7) == 0) {
                return j;
            }
        }
        return 5;
    }

    private static ByteBuf prepend(ByteBuf buf) {
        int length = buf.readableBytes();
        int a = a(length);

        ByteBuf prepended = buffer();
        prepended.ensureWritable(a + length);
        ByteBufHelper.writeVarInt(prepended, length);
        prepended.writeBytes(buf, buf.readerIndex(), length);
        return prepended;
    }

    private static ByteBuf transformFor(ByteBuf buf, AlixUser user) {
        return Dependencies.isAnyViaPresent ? ViaAccess.convert(buf, user.reetrooperUser().getClientVersion()) : buf;
    }

    private static ByteBuf compressByHandler(ByteBuf input, AlixUser user) {
        ChannelHandler compressor = user.getChannel().pipeline().get("compress");
        ChannelHandlerContext ctx = user.silentContext();
        ByteBuf temp = ctx.alloc().buffer();

        try {
            if (compressor != null) {
                CustomPipelineUtil.callEncode(compressor, ctx, input, temp);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            input.clear().writeBytes(temp);
            temp.release();
        }
        return input;
    }

    //From PacketCompressor
    private static ByteBuf compress(ByteBuf buf) {
        int length = buf.readableBytes();
        ByteBuf compressed = buffer();
        if (length < COMPRESSION_THRESHOLD) {
            ByteBufHelper.writeVarInt(compressed, 0);
            compressed.writeBytes(buf);
        } else {
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            ByteBufHelper.writeVarInt(compressed, length);

            Deflater compressor = new Deflater();
            compressor.setInput(bytes, 0, length);
            compressor.finish();

            byte[] temp = new byte[8192];

            while (!compressor.finished()) {
                int l = compressor.deflate(temp);
                compressed.writeBytes(temp, 0, l);
            }
        }
        return compressed;
    }*/

    private UnsafeNettyUtils() {
    }
}