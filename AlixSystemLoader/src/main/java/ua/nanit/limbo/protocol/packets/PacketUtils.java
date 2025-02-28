package ua.nanit.limbo.protocol.packets;

import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.connection.pipeline.compression.GlobalCompressionHandler;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;

public final class PacketUtils {

    public static void write(Channel channel, Version version, PacketSnapshot packet) {
        PacketDuplexHandler.write0(channel, packet, version, channel.voidPromise());
    }

    public static void writeAndFlush(Channel channel, Version version, PacketSnapshot packet) {
        write(channel, version, packet);
        channel.flush();
    }

    public static void closeWith(Channel channel, Version version, PacketSnapshot packet) {
        PacketDuplexHandler.write0(channel, packet, version, channel.newPromise()).addListener(ChannelFutureListener.CLOSE);
    }

    public static ByteBuf encode(Packet packet, boolean clientbound, Version version, CompressionHandler handler, boolean pooled) {
        return encode0(packet, clientbound, version, CompressionSupplier.supply(handler), pooled);
    }

    public static ByteBuf encode(Packet packet, boolean clientbound, Version version, boolean pooled) {
        return encode0(packet, clientbound, version, GlobalCompressionHandler::getCompressionFor, pooled);
    }

    private static ByteBuf encode0(Packet packet, boolean clientbound, Version version, CompressionSupplier compressSupplier, boolean pooled) {
        //boolean clientbound = side == PacketSide.CLIENT;
        for (State state : State.values()) {
            State.ProtocolMappings<? extends Packet> mappings = clientbound ? state.clientBound : state.serverBound;

            State.PacketRegistry registry = mappings.getRegistry(version);
            if (registry == null || !registry.hasPacket(packet.getClass())) continue;

            CompressionHandler handler = compressSupplier.getHandlerFor(packet, version, state);
            return PacketDuplexHandler.encodeToRaw0(packet, registry, version, handler, pooled);
        }

        throw new AlixException("Could not encode packet! Packet: " + packet.getClass().getSimpleName() + " clientbound: " + clientbound + " version: " + version + "!");
    }
}