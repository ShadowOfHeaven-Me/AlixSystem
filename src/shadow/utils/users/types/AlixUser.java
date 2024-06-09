package shadow.utils.users.types;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import shadow.utils.holders.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.netty.unsafe.raw.RawAlixPacket;
import shadow.utils.objects.packet.PacketProcessor;

public interface AlixUser {

    User reetrooperUser();

    PacketProcessor getPacketProcessor();

    boolean isVerified();

    Channel getChannel();

    ChannelHandlerContext silentContext();

    default boolean isAssignable(AlixUser user) {
        return this.silentContext() == user.silentContext();
    }

    default void writeDynamicMessageSilently(Component message) {
        //DEBUG_TIME();
        NettyUtils.writeDynamicWrapper(OutMessagePacketConstructor.packetWrapper(message), this.silentContext());
    }

    default void sendDynamicMessageSilently(String message) {
        //DEBUG_TIME();
        NettyUtils.writeAndFlushDynamicWrapper(OutMessagePacketConstructor.packetWrapper(Component.text(message)), this.silentContext());
    }

    //According to PacketTransformationUtil.transform(PacketWrapper<?>), this way of sending the packets silently is just fine
    default void writeDynamicSilently(PacketWrapper<?> packet) {
        NettyUtils.writeDynamicWrapper(packet, this.silentContext());
    }

    default void writeAndFlushDynamicSilently(PacketWrapper<?> packet) {
        //DEBUG_TIME();
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

    default void flushSilently() {
        //DEBUG_TIME();
        this.silentContext().flush();
    }

    default void writeConstSilently(ByteBuf buf) {
        //DEBUG_TIME();
        NettyUtils.writeConst(this.silentContext(), buf);
    }

    default void writeAndFlushConstSilently(ByteBuf buf) {
        //DEBUG_TIME();
        NettyUtils.writeAndFlushConst(this.silentContext(), buf);
    }
    
}