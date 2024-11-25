package nanolimbo.alix.connection.pipeline;

import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.SneakyThrows;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.connection.pipeline.compression.CompressionHandler;
import nanolimbo.alix.protocol.*;
import nanolimbo.alix.protocol.packets.login.PacketConfigDisconnect;
import nanolimbo.alix.protocol.registry.State;
import nanolimbo.alix.protocol.registry.Version;
import nanolimbo.alix.server.LimboServer;
import nanolimbo.alix.server.Log;

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
        ByteBuf buf = this.tryDecompress((ByteBuf) msg);
        if (buf == null) throw new AlixException("Invalid buf");

        //Log.error("VALID BUF - " + buf);

        //PacketDecoder
        Packet packet = PacketDecoder.decode(buf, this.decoderMappings, this.version);
        buf.release();
        if (packet == null) {
            buf.release();
            return;
        }

        Log.error("VALID PACKET - " + packet);

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
        Log.error("Error in pipeline", cause);
        this.connection.sendPacketAndClose(new PacketConfigDisconnect("§cInternal limbo error"));
    }

    //Compression - start

    public void tryEnableCompression() {
        //compression didn't exist before 1.8
        if (this.version.moreOrEqual(Version.V1_8)) this.enableCompression();
    }

    private void enableCompression() {
        //both of these need to be invoked eventLoop, writeAndFlushPacket already does the check
        this.writeAndFlushPacket(PacketSnapshots.SET_COMPRESSION);
        this.compression = CompressionHandler.getHandler(this.channel);
    }

    private ByteBuf tryDecompress(ByteBuf buf) throws Exception {
        //Log.error("COMPRESSION ENABLED READ: " + (this.compression != null));
        return this.compression != null ? this.compression.decompress(buf) : buf;
    }

    private ByteBuf tryCompress(ByteBuf buf) throws Exception {
        //Log.error("COMPRESSION ENABLED WRITE: " + (this.compression != null));
        return this.compression != null ? this.compression.compress(buf) : buf;
    }

    //Compression - end

    private void assertEventLoop() {
        if (!this.channel.eventLoop().inEventLoop()) throw new AlixException("Not in eventLoop");
    }

    public void flush() {
        this.assertEventLoop();
        this.channel.unsafe().flush();
    }

    public void writeAndFlushPacket(PacketOut packet) {
        this.writeAndFlushPacket(packet, this.channel.voidPromise());
    }

    public ChannelPromise writeAndFlushPacket(PacketOut packet, ChannelPromise promise) {
        this.writePacket(packet, promise);
        this.flush();
        return promise;
    }

    public void writePacket(PacketOut packet) {
        this.writePacket(packet, this.channel.voidPromise());
    }

    //actually does the encoding
    public static ByteBuf encodeToRaw0(PacketOut packet, State.PacketRegistry encoderMappings, Version version, CompressionHandler handler) throws Exception {
        //Prefix with packet Id and write contents
        ByteMessage byteMessage = PacketEncoder.encode(packet, encoderMappings, version);
        if (byteMessage == null) throw new AlixException("encodeToRaw() error");

        ByteBuf maybeCompressed = handler != null ? handler.compress(byteMessage.getBuf()) : byteMessage.getBuf();
        return VarIntLengthEncoder.encode(maybeCompressed);
    }

    @SneakyThrows
    @SuppressWarnings("UnusedReturnValue")
    public ChannelPromise writePacket(PacketOut packet, ChannelPromise promise) {
        Log.error("OUT: " + packet);
        this.assertEventLoop();

        ByteBuf buf;
        if (packet instanceof PacketSnapshot) buf = ((PacketSnapshot) packet).getCached(this.version);
        else buf = encodeToRaw0(packet, this.encoderMappings, this.version, this.compression);
        //Log.error("OUT BUF: " + Arrays.toString(new ByteMessage(buf).toByteArray()));

        this.channel.unsafe().outboundBuffer().addMessage(buf, buf.readableBytes(), promise);
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
}