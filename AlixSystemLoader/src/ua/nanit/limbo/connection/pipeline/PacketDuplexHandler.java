package ua.nanit.limbo.connection.pipeline;

import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.SneakyThrows;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.UnsafeCloseFuture;
import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.protocol.*;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

import java.io.IOException;

import static ua.nanit.limbo.connection.pipeline.compression.CompressionHandler.COMPRESSION_ENABLED;

public final class PacketDuplexHandler extends ChannelDuplexHandler {

    //private final FastCodecList codecList;
    private final Channel channel;
    private final LimboServer server;
    private CompressionHandler compression;
    private State.PacketRegistry encoderMappings, decoderMappings;
    private Version version;
    private ClientConnection connection;

    public PacketDuplexHandler(Channel channel, LimboServer server) {
        this.channel = channel;
        this.server = server;
        //this.codecList = FastCodecList.createNew();
        updateVersion(Version.getMin());
        updateState(State.HANDSHAKING);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //ByteBuf in = (ByteBuf) msg;

        ByteBuf buf = this.tryDecompress((ByteBuf) msg);
        if (buf == null) throw new AlixException("Invalid buf");

        //Log.error("VALID BUF - " + buf);

        //PacketDecoder
        Packet packet = PacketDecoder.decode(buf, this.decoderMappings, this.version);
        buf.release();
        if (packet == null) return;

        //Log.error("LIMBO IN: " + packet);
        packet.handle(this.connection, this.server);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Log.error("[BLOCKED] SERVER TRIED SENDING: " + msg);
        /*super.write(ctx, msg, promise);
        //Log.error("[BLOCKED] SERVER TRIED SENDING: " + msg);
        Packet packet = (Packet) msg;
        Log.error("OUT: " + packet.getClass().getSimpleName());

        //PacketEncoder
        ByteMessage byteMessage = PacketEncoder.encode(packet, this.encoderMappings, this.version);
        if (byteMessage == null) return;

        //VarIntLengthEncoder
        ByteBuf buf = VarIntLengthEncoder.encode(byteMessage);
        if (buf == null) {
            byteMessage.release();
            return;
        }

        this.channel.unsafe().outboundBuffer().addMessage(buf, buf.readableBytes(), promise);*/
    }

    /* @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        Log.error("HANDLER ADDED: " + ctx.pipeline().names());
    }*/


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //cause instanceof SocketException ||
        if (cause instanceof IOException) return;
        Log.error("Error in pipeline", cause);
        UnsafeCloseFuture.unsafeClose(ctx.channel());
        //this.connection.sendPacketAndClose(new PacketConfigDisconnect("§cInternal limbo error"));
    }

    //Compression - start

    public void tryEnableCompression() {
        //compression didn't exist before 1.8
        if (COMPRESSION_ENABLED && this.version.moreOrEqual(Version.V1_8)) this.enableCompression();
    }

    private void enableCompression() {
        //both of these need to be invoked on the eventLoop
        this.writeAndFlush(PacketSnapshots.SET_COMPRESSION);
        this.compression = CompressionHandler.getHandler(this.channel);
    }

    private ByteBuf tryDecompress(ByteBuf buf) throws Exception {
        //Log.error("COMPRESSION ENABLED READ: " + (this.compression != null));
        return this.compression != null ? this.compression.decompress(buf) : buf;
    }

    /*private ByteBuf tryCompress(ByteBuf buf) throws Exception {
        //Log.error("COMPRESSION ENABLED WRITE: " + (this.compression != null));
        return this.compression != null ? this.compression.compress(buf) : buf;
    }*/

    //Compression - end

    private void assertEventLoop() {
        if (!this.channel.eventLoop().inEventLoop()) throw new AlixException("Not in eventLoop");
    }

    public void flush() {
        //this.assertEventLoop();
        if (!this.channel.isActive()) return;
        this.channel.unsafe().flush();
    }

    public void writeAndFlush(PacketOut packet) {
        this.writeAndFlush(packet, this.channel.voidPromise());
    }

    public ChannelPromise writeAndFlush(PacketOut packet, ChannelPromise promise) {
        this.write(packet, promise);
        this.flush();
        return promise;
    }

    public void write(PacketOut packet) {
        this.write(packet, this.channel.voidPromise());
    }

    //actually does the encoding
    public static ByteBuf encodeToRaw0(PacketOut packet, State.PacketRegistry encoderMappings, Version version, CompressionHandler handler, boolean pooled) throws Exception {
        //Prefix with packet Id and write contents
        ByteMessage byteMessage = PacketEncoder.encode(packet, encoderMappings, version, pooled);
        if (byteMessage == null) throw new AlixException("encodeToRaw() error");

        ByteBuf maybeCompressed = handler != null ? handler.compress(byteMessage.getBuf()) : byteMessage.getBuf();
        return VarIntLengthEncoder.encode(maybeCompressed, pooled);
    }

    @SneakyThrows
    @SuppressWarnings("UnusedReturnValue")
    public ChannelPromise write(PacketOut packet, ChannelPromise promise) {
        if (!this.channel.isActive()) return promise;
        //Log.error("LIMBO OUT: " + packet);
        this.assertEventLoop();

        ByteBuf buf;
        if (packet instanceof PacketSnapshot) buf = ((PacketSnapshot) packet).getEncoded(this.version);
        else buf = encodeToRaw0(packet, this.encoderMappings, this.version, this.compression, true);
        //Log.error("OUT BUF: " + Arrays.toString(new ByteMessage(buf).toByteArray()));

        ChannelOutboundBuffer outboundBuffer = this.channel.unsafe().outboundBuffer();

        if (outboundBuffer != null) outboundBuffer.addMessage(buf, buf.readableBytes(), promise);
        else buf.release();

        return promise;
    }

    public void updateVersion(Version version) {
        this.version = version;
    }

    public void updateState(State state) {
        this.encoderMappings = state.clientBound.getRegistry(version);
        this.decoderMappings = state.serverBound.getRegistry(version);
    }

    public void updateEncoderState(State state) {
        this.encoderMappings = state.clientBound.getRegistry(version);
    }

    public void setClientConnection(ClientConnection connection) {
        this.connection = connection;
    }

    //prevent the events from being passed onto the other handlers

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }
}