package ua.nanit.limbo.protocol.packets;

import alix.common.utils.netty.FastNettyUtils;
import alix.common.utils.netty.WrapperUtils;
import alix.common.utils.other.throwable.AlixError;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.UnsafeCloseFuture;
import ua.nanit.limbo.connection.pipeline.PacketDecoder;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.connection.pipeline.compression.CompressionSupplier;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Log;

public final class PacketUtils {

    public static void debugBytes(ByteBuf buf) {
        StringBuilder sb = new StringBuilder(buf.readableBytes() * 3).append("[");

        int idx = buf.readerIndex();

        buf.forEachByte(0, buf.writerIndex(), b -> {
            sb.append(b).append(", ");
            return true;
        });
        if (idx != buf.readerIndex())
            throw new AlixError();

        sb.deleteCharAt(sb.length() - 1).append("]");
        Log.error("BYTES OF " + buf + ", =" + sb);
    }

    @SneakyThrows
    public static void validateOutCopy(ByteBuf out, CipherHandler cipher, CompressionHandler compress, State.PacketRegistry mappings, Version version) {
        if (out.readerIndex() != 0)
            throw new AlixError();

        if (NanoLimbo.debugBytes)
            debugBytes(out);

        ByteBuf copy = out.copy();
        decode(copy, cipher, compress, mappings, version);
    }

    public static void decode(ByteBuf buf, CipherHandler cipher, CompressionHandler compress, State.PacketRegistry mappings, Version version) throws Exception {
        //We cannot decrypt our own packets
        ByteBuf decrypted = CipherHandler.decrypt(buf, cipher);
        Log.error("DECRYPT BYTES:");
        PacketUtils.debugBytes(decrypted);

        int packetLen = FastNettyUtils.readVarInt(decrypted);
        ByteBuf decompressed = CompressionHandler.decompress(decrypted, compress);

        Packet packet = PacketDecoder.decode(decompressed, mappings, version, null);
        if (packet == null)
            throw new AlixError("No packet mapped");

        //the outgoing packet does not have a decode method impl
        if (decompressed.writerIndex() > 0 && decompressed.readerIndex() == 0) {
            var wrapper = WrapperUtils.alloc(decompressed, version.getRetrooperVersion(), State.getWrapperClazz(packet));
            wrapper.read();
        }
        decompressed.release();
    }

    public static void write(Channel channel, Version version, PacketSnapshot packet) {
        PacketDuplexHandler.write0(channel, packet, version, channel.voidPromise());
    }

    public static void writeAndFlush(Channel channel, Version version, PacketSnapshot packet) {
        write(channel, version, packet);
        channel.flush();
    }

    public static void closeWith(Channel channel, Version version, PacketSnapshot packet) {
        PacketDuplexHandler.write0(channel, packet, version, channel.newPromise()).addListener(UnsafeCloseFuture.INSTANCE);
    }

    public static ByteBuf encode(Packet packet, boolean clientbound, Version version, CompressionHandler handler, boolean pooled) {
        return encode0(packet, clientbound, version, CompressionSupplier.supply(handler), pooled);
    }

    public static ByteBuf encode(Packet packet, boolean clientbound, Version version, boolean pooled) {
        return encode0(packet, clientbound, version, CompressionSupplier.GLOBAL, pooled);
    }

    private static ByteBuf encode0(Packet packet, boolean clientbound, Version version, CompressionSupplier compressSupplier, boolean pooled) {
        //boolean clientbound = side == PacketSide.CLIENT;
        State state = State.getState(packet);
        State.ProtocolMappings<? extends Packet> mappings = clientbound ? state.clientBound : state.serverBound;

        State.PacketRegistry registry = mappings.getRegistry(version);
        if (registry == null || !registry.hasPacket(packet.getClass()))
            throw new AlixError("Could not encode packet! Packet: " + packet.getClass().getSimpleName() + " clientbound: " + clientbound + " version: " + version + "!");

        //State.getState()

        CompressionHandler handler = compressSupplier.getHandlerFor(packet, version, state);
        return PacketDuplexHandler.encodeToRaw0(packet, registry, version, handler, pooled);
    }
}