package shadow.utils.users.types;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.netty.unsafe.raw.RawAlixPacket;
import shadow.utils.objects.packet.PacketProcessor;

public interface AlixUser {

    User reetrooperUser();

    PacketProcessor getPacketProcessor();

    boolean isVerified();

    Channel getChannel();

    ChannelHandlerContext silentContext();

    //does not account for the TemporaryUser
    default boolean isAssignable(AlixUser user) {
        return this.silentContext() == user.silentContext();
    }

/*    static void DEBUG_TIME() {
        StringBuilder sb = new StringBuilder("START");
        int c = 0;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            sb.append('\n').append(element);
            if (++c == 10) {
                sb.append('\n').append("(almost) END");
                break;
            }
        }
        Main.logError("INVOKED STACKTRACE (send this to shadow): " + sb);
    }*/

    default void writeDynamicMessageSilently(Component message) {
        NettyUtils.writeDynamicWrapper(OutMessagePacketConstructor.packetWrapper(message), this.silentContext());
    }

    default void sendDynamicMessageSilently(String message) {
        NettyUtils.writeAndFlushDynamicWrapper(OutMessagePacketConstructor.packetWrapper(Component.text(message)), this.silentContext());
    }

    //According to PacketTransformationUtil.transform(PacketWrapper<?>), this way of sending the packets silently is just fine
    default void writeDynamicSilently(PacketWrapper<?> packet) {
        NettyUtils.writeDynamicWrapper(packet, this.silentContext());
    }

    default void writeAndFlushDynamicSilently(PacketWrapper<?> packet) {
        NettyUtils.writeAndFlushDynamicWrapper(packet, this.silentContext());
    }

    default void writeRaw(ByteBuf rawBuffer) {
        //DEBUG_TIME();
        RawAlixPacket.writeRaw(rawBuffer, this.getChannel());
    }

    default void writeAndFlushRaw(ByteBuf rawBuffer) {
        //DEBUG_TIME();
        this.writeRaw(rawBuffer);
        this.getChannel().unsafe().flush();
    }

    default void writeSilently(ByteBuf buffer) {
        //DEBUG_TIME();
        this.silentContext().write(buffer);
    }

    default void writeAndFlushSilently(ByteBuf buffer) {
        //DEBUG_TIME();
        this.silentContext().writeAndFlush(buffer);
    }

    default void flush() {
        //DEBUG_TIME();
        //this.silentContext().flush();
        this.getChannel().flush();
    }

    default void writeConstSilently(ByteBuf buf) {
        NettyUtils.writeConst(this.silentContext(), buf);
    }

    default void writeAndFlushConstSilently(ByteBuf buf) {
        NettyUtils.writeAndFlushConst(this.silentContext(), buf);
    }
}