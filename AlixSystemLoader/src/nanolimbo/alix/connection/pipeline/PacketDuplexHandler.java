package nanolimbo.alix.connection.pipeline;

import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.SneakyThrows;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.connection.PacketSnapshots;
import nanolimbo.alix.connection.pipeline.compression.CompressionHandler;
import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.Packet;
import nanolimbo.alix.protocol.packets.login.PacketDisconnect;
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

        //Log.error("VALID BUF - " + buf);

        //PacketDecoder
        Packet packet = PacketDecoder.decode(buf, this.decoderMappings, this.version);
        if (packet == null) {
            buf.release();
            return;
        }

        //Log.error("VALID PACKET - " + packet);
        try {
            packet.handle(this.connection, this.server);
        } finally {
            buf.release();
        }
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
        connection.sendPacketAndClose(new PacketDisconnect(""));
    }

    //Compression - start

    public void tryEnableCompression() {
        //compression didn't exist before 1.8
        if (this.version.moreOrEqual(Version.V1_8)) this.enableCompression();
    }

    private void enableCompression() {
        this.writeAndFlushPacket(PacketSnapshots.SET_COMPRESSION);
        this.compression = new CompressionHandler(this.channel);
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

    public void writeAndFlushPacket(Packet packet) {
        this.writeAndFlushPacket(packet, this.channel.voidPromise());
    }

    public ChannelPromise writeAndFlushPacket(Packet packet, ChannelPromise promise) {
        this.writePacket(packet, promise);
        this.flush();
        return promise;
    }

    public void writePacket(Packet packet) {
        this.writePacket(packet, this.channel.voidPromise());
    }

    @SneakyThrows
    @SuppressWarnings("UnusedReturnValue")
    public ChannelPromise writePacket(Packet packet, ChannelPromise promise) {
        //Log.error("OUT: " + packet);
        this.assertEventLoop();

        //Prefix with packet Id and write contents
        ByteMessage byteMessage = PacketEncoder.encode(packet, this.encoderMappings, this.version);
        if (byteMessage == null) return promise;

        ByteBuf buf = VarIntLengthEncoder.encode(this.tryCompress(byteMessage.getBuf()));
        /*if (buf == null) {
            byteMessage.release();
            return promise;
        }*/

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