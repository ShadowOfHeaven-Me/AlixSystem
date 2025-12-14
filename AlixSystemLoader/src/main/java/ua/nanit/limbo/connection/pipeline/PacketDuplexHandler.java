package ua.nanit.limbo.connection.pipeline;

import alix.common.utils.netty.NettySafety;
import alix.common.utils.other.annotation.AlixIntrinsified;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.SneakyThrows;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.UnsafeCloseFuture;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.connection.pipeline.flush.FlushBatcher;
import ua.nanit.limbo.protocol.*;
import ua.nanit.limbo.protocol.packets.shadow.KeepAlivePacket;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static ua.nanit.limbo.connection.pipeline.compression.CompressionHandler.COMPRESSION_ENABLED;
import static ua.nanit.limbo.protocol.PacketSnapshot.floodgateNoCompression;

public final class PacketDuplexHandler extends ChannelDuplexHandler {

    //private final FastCodecList codecList;
    private final Channel channel;
    private final LimboServer server;
    private final ClientConnection connection;
    private final FlushBatcher flushBatcher;
    private final ChannelPromise voidPromise;
    public final boolean isGeyser, passPayloads;
    public boolean disablePacketWriting;
    private CipherHandler cipher;
    private CompressionHandler compression;
    private State.PacketRegistry encoderMappings, decoderMappings;

    public PacketDuplexHandler(Channel channel, LimboServer server, ClientConnection connection) {
        this.channel = channel;
        this.server = server;
        this.connection = connection;
        this.flushBatcher = FlushBatcher.implFor(channel);
        this.voidPromise = channel.voidPromise();
        var geyserUtil = this.server.getIntegration().geyserUtil();
        this.isGeyser = geyserUtil.isBedrock(channel);
        this.passPayloads = this.isGeyser && geyserUtil.isFloodgatePresent();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = this.tryDecompress((ByteBuf) msg);
        if (buf == null)
            throw NettySafety.INVALID_BUF;

        //Log.error("VALID BUF - " + buf);

        //PacketDecoder
        Packet packet = PacketDecoder.decode(buf, this.decoderMappings, this.version(), this.connection);
        buf.release();
        if (packet == null) return;

        if (NanoLimbo.debugPackets)
            Log.error("LIMBO IN: " + packet);

        /*if (this.passPayloads && packet.getClass() == PacketPlayInPluginMessage.class) {
            PacketPlayInPluginMessage pluginMessage = (PacketPlayInPluginMessage) packet;

            this.server.getIntegration().fireCustomPayloadEvent(this.connection, pluginMessage.wrapper().getChannelName(), pluginMessage.wrapper().getData());

            //ByteBuf pluginMessageBuf = BufUtils.pooledBuffer();
            //var wrapper = new WrapperConfigClientPluginMessage(pluginMessage.wrapper().getChannelName(), pluginMessage.wrapper().getData());
            //WrapperUtils.writeWithID(wrapper, pluginMessageBuf, this.version().getRetrooperVersion());

            //this.channel.pipeline().context(PacketEvents.DECODER_NAME).fireChannelRead(pluginMessageBuf);
            //getBefore(channel, this.channel.pipeline().context(PacketEvents.DECODER_NAME)).fireChannelRead(pluginMessageBuf);

            //Log.error("receivePacketSilently: " + pluginMessage.wrapper().getChannelName() + " NAMES: " + channel.pipeline().names());
        }*/

        this.flushBatcher.readBegin();
        try {
            packet.handle(this.connection, this.server);
        } catch (Exception e) {
            this.connection.closeInvalidPacket();
            if (NanoLimbo.suppress(e)) return;

            Log.error("Error during packet handle", e);
        } finally {
            this.flushBatcher.readComplete();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (NanoLimbo.debugPackets)
            Log.error("SERVER SENT " + msg, new AlixError());

        super.write(ctx, msg, promise);
    }


    /*    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //Log.error("[BLOCKED] SERVER TRIED SENDING: " + msg);
        //ReferenceCountUtil.release(msg);

        Log.error("SERVER SENT: " + msg);
        super.write(ctx, msg, promise);
        *//*
        //Log.error("[BLOCKED] SERVER TRIED SENDING: " + msg);
        Packet packet = (Packet) msg;
        Log.error("OUT: " + packet.getClass().getSimpleName());

        //PacketEncoder
        ByteMessage byteMessage = PacketEncoder.encode(packet, this.encoderMappings, this.version());
        if (byteMessage == null) return;

        //VarIntLengthEncoder
        ByteBuf buf = VarIntLengthEncoder.encode(byteMessage);
        if (buf == null) {
            byteMessage.release();
            return;
        }

        this.channel.unsafe().outboundBuffer().addMessage(buf, buf.readableBytes(), promise);*//*
    }*/

    /* @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        Log.error("HANDLER ADDED: " + ctx.pipeline().names());
    }*/


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //cause instanceof SocketException ||
        if (cause instanceof IOException) return;

        this.connection.closeInvalidPacket();
        if (NanoLimbo.suppress(cause)) return;

        Log.error("Error in pipeline", cause);
        UnsafeCloseFuture.unsafeClose(ctx.channel());
        //this.connection.sendPacketAndClose(new PacketConfigDisconnect("§cInternal limbo error"));
    }

    //Compression - start

/*    public boolean tryEnableCompression() {
        return this.tryEnableCompression(true);
    }*/

    public boolean tryEnableCompression(boolean sendPacket, boolean forceful) {
        //compression didn't exist before 1.8
        boolean canEnable = COMPRESSION_ENABLED && !this.disableCompression() && this.version().moreOrEqual(Version.V1_8);
        if (canEnable || forceful) {
            if (!canEnable) {
                Log.warning("Forcefully enabling compression, even tho player " + this.connection.getUsername()
                        + " should not have it enabled! DEBUG: " +
                        "\nCOMPRESSION_ENABLED=" + COMPRESSION_ENABLED +
                        "\nCOMPRESS THRESHOLD=" + NanoLimbo.INTEGRATION.getCompressionThreshold() +
                        "\nthis.disableCompression()=" + this.disableCompression() +
                        "\nFloodgateNoCompression=" + floodgateNoCompression +
                        "\nisGeyser=" + isGeyser +
                        "\ngeyser player=" + NanoLimbo.INTEGRATION.geyserUtil().getBedrockPlayer(this.channel) +
                        "\nVERSION=" + this.version());
            }

            this.enableCompression(sendPacket);
            return true;
        }
        return false;
    }

    private void enableCompression(boolean sendPacket) {
        //both of these need to be invoked on the eventLoop
        if (sendPacket) this.writeAndFlush(PacketSnapshots.SET_COMPRESSION);
        this.compression = CompressionHandler.getHandler(this.channel);
    }

    private ByteBuf tryDecompress(ByteBuf buf) throws Exception {
        //Log.error("COMPRESSION ENABLED READ: " + (this.compression != null));
        return CompressionHandler.decompress(buf, this.compression);
    }

    private ByteBuf tryEncrypt(ByteBuf buf) {
        try {
            return this.cipher != null ? this.cipher.encrypt(buf) : buf;
        } catch (Exception e) {
            throw new AlixError(e);
        }
    }
    /*private ByteBuf tryCompress(ByteBuf buf) throws Exception {
        //Log.error("COMPRESSION ENABLED WRITE: " + (this.compression != null));
        return this.compression != null ? this.compression.compress(buf) : buf;
    }*/

    //Compression - end

    public boolean inEventLoop() {
        return this.channel.eventLoop().inEventLoop();
    }

    private void assertEventLoop() {
        if (!this.inEventLoop()) throw new AlixException("Not in eventLoop");
    }

    public void flush() {
        this.flushBatcher.flush();
    }

    public void writeAndFlush(PacketOut packet) {
        this.writeAndFlush(packet, this.voidPromise);
    }

    public ChannelPromise writeAndFlush(PacketOut packet, ChannelPromise promise) {
        this.write(packet, promise);
        this.flush();
        return promise;
    }

    private static final KeepAlivePacket sex = KeepAlivePacket.sex(67);
    private long delay;

    private void delayed(Runnable r) {
        this.channel.eventLoop().schedule(() -> {
            if (!this.channel.isOpen()) {
                return;
            }
            r.run();
            this.write0(sex.getPacket(this.connection), this.channel.voidPromise());
            FlushBatcher.flush0(this.channel);
        }, this.delay, TimeUnit.SECONDS);
        this.delay += 2;
    }

    public void write(PacketOut packet) {
        this.write(packet, this.voidPromise);
    }

    //actually does the encoding
    @SneakyThrows
    public static ByteBuf encodeToRaw0(Packet packet, State.PacketRegistry encoderMappings, Version version, CompressionHandler handler, boolean pooled) {
        //Prefix with packet Id and write contents
        ByteMessage byteMessage = PacketEncoder.encode(packet, encoderMappings, version, pooled);
        if (byteMessage == null) throw new AlixException("encodeToRaw() error");

        ByteBuf maybeCompressed = handler != null ? handler.compress(byteMessage.getBuf()) : byteMessage.getBuf();

        return VarIntLengthEncoder.encode(maybeCompressed, pooled);
    }

    public boolean disableCompression() {
        return this.isGeyser && floodgateNoCompression;
    }

    @AlixIntrinsified(method = "ChannelOutboundInvoker::write")
    public ChannelPromise write(PacketOut packet, ChannelPromise promise) {
        if (!this.channel.isActive()) return promise;
        if (disablePacketWriting) return promise.setSuccess();

        if (NanoLimbo.debugPackets) {
            StringBuilder sb = new StringBuilder(" ");

            if (this.cipher != null)
                sb.append("[ENCRYPTED] ");
            if (this.compression != null)
                sb.append("[COMPRESSED]");

            Log.error("LIMBO OUT: " + packet + sb);
        }
            /*String str = packet.toString();

            if (str.equals("PacketPlayOutInventoryItems")) {
                Log.error("trace: ");
                new Exception().printStackTrace();
            }*/

        this.assertEventLoop();
        this.write0(packet, promise);
        return promise;
    }

    private void write0(PacketOut packet, ChannelPromise promise) {
        ByteBuf buf;
        State state;
        if (packet instanceof PacketSnapshot snapshot) {
            if (this.disableCompression()) buf = snapshot.getEncodedNoCompression(this.version());
            else buf = snapshot.getEncoded(this.version());

            state = snapshot.state;
        } else {
            buf = encodeToRaw0(packet, this.encoderMappings, this.version(), this.compression, true);
            state = State.getState(packet);
        }

        if (this.connection.getEncoderState() != state)
            throw new AlixError("co tu się kurwa odpierdala - " + packet + " encoder=" + this.connection.getEncoderState() + " packet=" + state);

        //Log.error("OUT BUF: " + Arrays.toString(new ByteMessage(buf).toByteArray()));

        //Log.error("PRE TO ENCRYPT BYTES:");
        //PacketUtils.debugBytes(buf);

        ByteBuf out = this.tryEncrypt(buf);
        ChannelOutboundBuffer outboundBuffer = this.channel.unsafe().outboundBuffer();

        /*if (NanoLimbo.debugPackets && false)
            PacketUtils.validateOutCopy(out, this.cipher, this.compression, this.encoderMappings, this.version());*/

        if (outboundBuffer != null) outboundBuffer.addMessage(out, out.readableBytes(), promise);
        else out.release();
    }

    public static ChannelPromise write0(Channel channel, PacketSnapshot packet, Version version, ChannelPromise promise) {
        if (!channel.eventLoop().inEventLoop()) {
            channel.close();
            throw new AlixError("write0 not in EventLoop");
        }

        if (NanoLimbo.debugCipher && channel.pipeline().context("cipher-encoder") != null) {
            throw new AlixError("debugCipher: " + channel.pipeline().names());
        }

        if (NanoLimbo.debugPackets) Log.error("write0 - " + packet.getPacket());

        boolean noCompression = floodgateNoCompression && NanoLimbo.INTEGRATION.geyserUtil().isBedrock(channel);
        ByteBuf buf = noCompression ? packet.getEncoded(version) : packet.getEncodedNoCompression(version);
        ChannelOutboundBuffer outboundBuffer = channel.unsafe().outboundBuffer();

        if (outboundBuffer != null) outboundBuffer.addMessage(buf, buf.readableBytes(), promise);
        //else buf.release();

        return promise;
    }

    public void updateState(State state) {
        this.encoderMappings = state.clientBound.getRegistry(this.version());
        this.decoderMappings = state.serverBound.getRegistry(this.version());
    }

    public void updateEncoderState(State state) {
        this.encoderMappings = state.clientBound.getRegistry(this.version());
    }

    private Version version() {
        return this.connection.getClientVersion();
    }

    public void setCipher(CipherHandler cipher) {
        this.cipher = cipher;
    }

    public CompressionHandler compressionHandler() {
        return this.compression;
    }

    //make sure to optimize all flushes, even indirect (? - not sure about this)

    /*@Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        this.flushBatcher.flush();
    }*/

    //prevent the events from being passed onto the other handlers

    private boolean passRegistration() {
        return this.connection.getVerifyState() instanceof LoginState;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (this.passRegistration())
            super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (this.passRegistration())
            super.channelActive(ctx);
    }

    //DO NOT BLOCK channelInactive

    /*@Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
    }*/

    /*@Override
    public void channelInactive(ChannelHandlerContext ctx) {
    }*/
}