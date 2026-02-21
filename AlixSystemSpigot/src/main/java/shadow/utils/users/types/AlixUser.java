package shadow.utils.users.types;

import alix.spigot.api.users.AbstractSpigotUser;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import shadow.Main;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.netty.unsafe.ByteBufHarvester;
import shadow.utils.netty.unsafe.raw.RawAlixPacket;
import shadow.utils.objects.packet.PacketProcessor;

import java.util.function.Consumer;

public interface AlixUser extends AbstractSpigotUser {

    @Override
    User retrooperUser();

    PacketProcessor getPacketProcessor();

    @Override
    boolean isVerified();

    @Override
    Channel getChannel();

    ChannelHandlerContext silentContext();

    //ByteBufHarvester bufHarvester();

    //does not account for TemporaryUser
    default boolean isAssignable(AlixUser user) {
        return this.silentContext() == user.silentContext();
    }

    static void DEBUG_TIME() {
        /*StringBuilder sb = new StringBuilder("START");
        int c = 0;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            sb.append('\n').append(element);
            if (++c == 10) {
                sb.append('\n').append("(almost) END");
                break;
            }
        }
        Main.logError("INVOKED STACKTRACE (send this to shadow): " + sb);*/
    }

    default void writeAndOccasionallyFlushSilently(ByteBuf[] buffers) {
        this.writeAndFlushWithThresholdSilently(buffers, 100);
    }

    default void writeAndFlushWithThresholdSilently(ByteBuf[] buffers, int threshold) {
        this.writeAndFlushWithThresholdSilently(buffers, threshold, this::writeSilently);
    }

    default void writeAndFlushWithThresholdSilently(ByteBuf[] buffers, int threshold, Consumer<ByteBuf> writeFunction) {
        for (int c = 1; c <= buffers.length; c++) {
            ByteBuf buf = buffers[c - 1];
            writeFunction.accept(buf);
            if (c % threshold == 0) this.flush();
        }
        if (buffers.length % threshold != 0)//last loop was not a flush
            this.flush();
    }

    default void writeDynamicMessageSilently(Component message) {
        NettyUtils.writeDynamicWrapper(OutMessagePacketConstructor.packetWrapper(message), this.silentContext());
    }

    default void sendDynamicMessageSilently(String message) {
        NettyUtils.writeAndFlushDynamicWrapper(OutMessagePacketConstructor.packetWrapper(Component.text(message)), this.silentContext());
    }

    default void debug(String message) {
        this.sendDynamicMessageSilently(message);
        Main.debug(message);
    }

    //According to PacketTransformationUtil.transform(PacketWrapper<?>), this way of sending the packets silently is just fine
    default void writeDynamicSilently(PacketWrapper<?> packet) {
        NettyUtils.writeDynamicWrapper(packet, this.silentContext());
    }

    default void writeAndFlushDynamicSilently(PacketWrapper<?> packet) {
        NettyUtils.writeAndFlushDynamicWrapper(packet, this.silentContext());
    }

    default void writeRaw(ByteBuf rawBuffer) {
        DEBUG_TIME();
        RawAlixPacket.writeRaw(rawBuffer, this.getChannel());
    }

    default void writeAndFlushRaw(ByteBuf rawBuffer) {
        DEBUG_TIME();
        this.writeRaw(rawBuffer);
        this.flush();
        //this.getChannel().unsafe().flush();
    }

    default void writeSilently(ByteBuf buffer) {
        DEBUG_TIME();
        this.silentContext().write(buffer);
    }

    default void writeAndFlushSilently(ByteBuf buffer) {
        DEBUG_TIME();
        this.silentContext().writeAndFlush(buffer);
    }

    default void flush() {
        DEBUG_TIME();
        //this.silentContext().flush();
        this.getChannel().flush();
    }

    default void writeConstAndFlushSilently(ByteBuf[] bufs) {
        for (ByteBuf buf : bufs) this.writeConstSilently(buf);
        this.flush();
    }

    default void writeConstSilently(ByteBuf buf) {
        NettyUtils.writeConst(this.silentContext(), buf);
    }

    default void writeAndFlushConstSilently(ByteBuf buf) {
        NettyUtils.writeAndFlushConst(this.silentContext(), buf);
    }
}