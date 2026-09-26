package ua.nanit.limbo.connection.pipeline;

import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.netty.BufUtils;
import alix.common.utils.netty.safety.NettySafety;
import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder.Cumulator;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.UnsafeCloseFuture;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.server.Log;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.ByteToMessageDecoder.MERGE_CUMULATOR;

public final class VarIntFrameDecoder extends ChannelInboundHandlerAdapter {

    //Original: https://github.com/Nan1t/NanoLimbo/blob/main/src/main/java/ua/nanit/limbo/connection/pipeline/VarIntFrameDecoder.java
    //Optimized with: https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/netty/FallbackVarInt21FrameDecoder.java#L69

    //private static final Boolean PRESENT = Boolean.TRUE;
    //private boolean collectResend = true;
    private CipherHandler cipher;
    //private final Map<ByteBuf, Integer> resendMap = new IdentityHashMap<>(2);
    //private final FixedSizeQueue<ByteBuf> collectedIdentity = new FixedSizeQueue<>(2);
    //private final FixedSizeQueue<ByteBuf> resendCopies = new FixedSizeQueue<>(2);
    //private final Deque<ByteBuf> resend = new ArrayDeque<>(2);

    //must be an IdentityHashMap, cuz the ByteBuf#hashCode changes depending on its readerIndex
    //private final Set<ByteBuf> resend = Collections.newSetFromMap(new IdentityHashMap<>(2));
    private final ClientConnection connection;

    public VarIntFrameDecoder(ClientConnection connection) {
        this.connection = connection;
    }
    //private final BufSet12 resend = new BufSet12();

    //private final VarIntByteDecoder reader = new VarIntByteDecoder();

    public void stopResendCollection() {
        //this.collectResend = false;
    }

    //cheap thread-local check
    //boolean released;

    public void releaseCollected() {
        /*if (released) return;

        this.released = true;
        this.forEachCollected(BufRelease::safeDynamicRelease);
        *//*this.forEachCollected(buf -> {
            int refCnt = buf.refCnt();
            if (refCnt != 0) buf.release(refCnt);
        });*/
        var msg = this.haProxyMessage;
        if (msg != null)
            ReferenceCountUtil.release(msg);
    }

    /*public void resendCollected(Channel channel) {
        this.forEachCollected(buf -> channel.pipeline().fireChannelRead(buf));
    }*/

    /*public void resendCollected() {
     *//*this.forEachCollected(buf ->
                channel.pipeline().fireChannelRead(buf.readerIndex(0)));*//*
        //readerIndex is already set by BufSet12
        this.forEachCollected(buf -> this.connection.getChannel().pipeline().fireChannelRead(buf.readerIndex(0)));
    }

    private void forEachCollected(Consumer<ByteBuf> consumer) {
        this.resend.forEach(consumer);
    }*/

    public void setCipher(CipherHandler cipher) {
        this.cipher = cipher;
    }

    private ByteBuf tryDecrypt(ByteBuf buf) throws Exception {
        return CipherHandler.decrypt(buf, this.cipher);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        //just being extra safe here
        var e = ctx.channel().eventLoop();

        if (e.inEventLoop()) this.cleanUp();
        else {
            //normally, a terrible idea, here however, should be good enough
            //if (this.cumulation != null)
            e.execute(this::cleanUp);
        }
    }

    private void cleanUp() {
        /*if (!this.connection.getChannel().eventLoop().inEventLoop())
            throw new AlixError("AAAAAAA " + Thread.currentThread());*/

        ByteBuf cum = this.cumulation;
        if (cum == null) return;

        this.cumulation = null;

        //(no need now, since executed on event loop)
        BufUtils.safeRelease(cum);
    }

    public static final AttributeKey<InetSocketAddress> PROXY_ADDRESS_KEY = AttributeKey.newInstance("alix-proxy-ip-addr");
    public volatile HAProxyMessage haProxyMessage;

    void handleProxyMessage(HAProxyMessage message) {
        var channel = this.connection.getChannel();
        LimboJoinProfiler.update(channel, ConnectionStage.HA_PROXY_MESSAGE_RECEIVED);

        this.haProxyMessage = message;
        if (message.command() != HAProxyCommand.PROXY)
            return;

        try {
            var addr = new InetSocketAddress(message.sourceAddress(), message.sourcePort());
            channel.attr(PROXY_ADDRESS_KEY).set(addr);
            LimboJoinProfiler.update(channel, ConnectionStage.HA_PROXY_ADDRESS_ASSIGNED);

            NanoLimbo.INTEGRATION.onProxyAddress(channel, addr);
        } catch (Exception e) {
            UnsafeCloseFuture.unsafeClose(channel);
            throw new AlixException(e);
        }
    }

    private final Cumulator cumulator = MERGE_CUMULATOR;
    private ByteBuf cumulation;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (NanoLimbo.debugFrames)
            Log.error("FRAME=" + msg);
        //Log.error("PIPELINE= " + ctx.channel().pipeline().names());

        //two volatile reads should be good enough perf here, since arriving packets should be large enough to overshadow this minor perf dent
        if (!ctx.channel().isActive() || ctx.isRemoved()) {
            this.cleanUp();
            ReferenceCountUtil.release(msg);
            return;
        }

        if (msg instanceof HAProxyMessage message) {
            this.handleProxyMessage(message);
            return;
        }

        if (!(msg instanceof ByteBuf buf))
            return;

        ByteBuf in = this.tryDecrypt(buf);

        if (NanoLimbo.debugBytes)
            PacketUtils.debugBytes(in);

        //From ByteToMessageDecoder
        if (this.cumulation == null) this.cumulation = in;
        else {
            //TODO: Add a safety mechanism to prevent invalid packets from overflowing the buffer
            this.cumulation = this.cumulator.cumulate(BufUtils.POOLED, this.cumulation, in);
        }

        while (this.cumulation.isReadable()) {
            //get the current frame
            ByteBuf decoded = this.decode(this.cumulation);
            if (decoded == null)
                break;//nothing left to decode (or nothing was decoded at all)

            super.channelRead(ctx, decoded);

            if (this.cumulation == null)
                return;//Can become null inside super.channelRead(...)
        }

        if (!this.cumulation.isReadable()) {
            if (this.cumulation.refCnt() != 0)//how tf does this happen
                this.cumulation.release();

            this.cumulation = null;
        } else if (this.cumulation.readerIndex() > 1024) {
            //is discardSomeReadBytes a better alternative here?
            this.cumulation.discardReadBytes();
        }
    }

    private static final boolean supportHttp = ConfigProvider.config.getBoolean("support-mc-port-http");

    //@Override
    private ByteBuf decode(ByteBuf in) {
        int packetStart = in.readerIndex();
        in.markReaderIndex();

        //has at least 2 bytes - packet len & packet id
        if (in.readableBytes() < 2) return null;

        //make sure not to firewall legacy ping, since still sent by modern clients in case of no-response after a 30-second timeout
        if (this.connection.getDecoderState() == State.HANDSHAKING && in.readableBytes() >= 3
            && BufUtils.hasSequence(in, packetStart, 0xFE, 0x01, 0xFA)) {
            this.connection.close();
            return null;
        }

        //(NanoLimbo.INTEGRATION.isProxyProtocol() ||
        //cannot do this \/
        /*&& NanoLimbo.INTEGRATION.isConnected(AlixCommonUtils.getAddress(this.connection.getChannel()))*/
        if (supportHttp && this.connection.getDecoderState() == State.HANDSHAKING) {
            if (in.readableBytes() < 4)
                return null;

            if (BufUtils.hasSequence(in, packetStart, 'G', 'E', 'T')//this is actually sent by an mc client
                || BufUtils.hasSequence(in, packetStart, 'H', 'E', 'A', 'D')) {
                //Log.info("Passthrough of HTTP traffic");
                this.connection.uninjectWithBuf(this.cumulation.readerIndex(0).copy());
                return null;
            }
        }

        int len = readVarIntPacketLength(in);
        NettySafety.validateUserInputBufAlloc(len);
        //uhh, is it possible for this to be the result of fragmentation?
        //if (len < 0) throw NettySafety.INVALID_PACKET_LEN;

        //0 here means "not enough bytes yet to finish reading the length VarInt" (see readVarInt2Byte's own
        //atStop==0 case - its only reachable caller, given the readableBytes()>=2 guard above), NOT an
        //actual invalid VarInt - a genuinely malformed/oversized VarInt already throws NettySafety
        //.INVALID_VAR_INT directly from readVarInt3Or4Byte instead of returning 0. Treating 0 as invalid
        //here disconnected any legitimate client whose packet length needs a 3rd length-prefix byte (any
        //packet body 16384-32767 bytes, well within MAX_RECEIVED_SIZE) if TCP happened to split the length
        //prefix across two reads - feeding a false "invalid packet" signal into the anomaly detector for
        //ordinary network jitter.
        if (len == 0) {
            in.resetReaderIndex();
            return null;
        }

        //the packet is said to be larger than what we've cumulated (hehe) so far
        if (len > in.readableBytes()) {
            in.resetReaderIndex();
            return null;
        }
        //Log.error("PACKET START: " + packetStart + " IDX: " + in.readerIndex() + " notReadYet: " + notReadYet);

        /*if (collectResend && !this.resend.contains(in))// && notReadYet) {
            this.resend.add(in.retain());*/

        //Log.error("IN BUF COUNT: " + resend.size() + " === " + in + " HASH: " + System.identityHashCode(in));

        try {
            return in.readRetainedSlice(len);
        } catch (Throwable ex) {
            this.connection.closeInvalidPacket();
            if (NanoLimbo.suppress(ex)) return null;

            Log.error("len=" + len + " rdx=" + in.readerIndex() + " rby=" + in.readableBytes(), ex);
        }

        //Unnecessary: ByteBuf#slice does not allocate a new buffer (as in, space)
//        //Optimize: Reduce ByteBuf creation
//        //Do not create a separate ByteBuf for reading the very last packet in the received ByteBuf
//        //This is fine, since we do not rely on the capacity() or any similar methods, and instead
//        //only do reading operations
//        boolean isLastBuf = in.readableBytes() == len;
//
//        if (isLastBuf) out.add(in.retain());
//        else out.add(in.readRetainedSlice(len));
        return null;
    }

    private static int readVarIntPacketLength(ByteBuf buf) {
        switch (buf.readableBytes()) {
            case 2:
                return readVarInt2Byte(buf);
            case 1: {
                byte val = buf.readByte();
                //check if it has the continuation bit set
                if ((val & 0x80) != 0) return 0;
                return val;
            }
            case 0:
                return 0;
            //case 3:
            default:
                return readVarInt3Or4Byte(buf, buf.getMediumLE(buf.readerIndex()));
        }
    }

    //can't really be 4 bytes in minecraft, but whatever
    private static int readVarInt3Or4Byte(final ByteBuf buf, final int wholeOrMore) {
        // Read 3 bytes in little-endian order
        final int atStop = ~wholeOrMore & 0x808080; // Check for stop bits

        // If no stop bits are found, throw an exception
        if (atStop == 0) throw NettySafety.INVALID_VAR_INT;

        // Find the position of the first stop bit
        final int bitsToKeep = Integer.numberOfTrailingZeros(atStop) + 1;
        buf.skipBytes(bitsToKeep >> 3); // Skip the processed bytes

        // Extract and preserve the valid bytes
        int preservedBytes = wholeOrMore & (atStop ^ (atStop - 1));

        // Compact the 7-bit chunks
        preservedBytes = (preservedBytes & 0x007F007F) | ((preservedBytes & 0x00007F00) >> 1);
        preservedBytes = (preservedBytes & 0x00003FFF) | ((preservedBytes & 0x3FFF0000) >> 2);

        return preservedBytes;
    }

    private static int readVarInt2Byte(final ByteBuf buf) {
        // Read 2 bytes in little-endian order
        final int wholeOrMore = buf.getShortLE(buf.readerIndex()); // Reads 2 bytes as an integer
        final int atStop = ~wholeOrMore & 0x8080; // Identify stop bits in the two bytes

        // If no stop bits are found, the VarInt is too large
        if (atStop == 0) return 0;

        // Find the first stop bit
        final int bitsToKeep = Integer.numberOfTrailingZeros(atStop) + 1;
        buf.skipBytes(bitsToKeep >> 3); // Skip the number of processed bytes

        // Extract and preserve the relevant 7-bit chunks
        int preservedBytes = wholeOrMore & (atStop ^ (atStop - 1));

        // Compact the 7-bit chunks into a single integer
        preservedBytes = (preservedBytes & 0x007F) | ((preservedBytes & 0x7F00) >> 1);
        return preservedBytes;
    }

/*    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        //Log.error("READER IDX=0: " + readerIndex0);

        this.reader.reset();
        int varIntEnd = in.forEachByte(reader);

        if (varIntEnd == -1) return;
        boolean readerIndex0 = in.readerIndex() == 0;

        switch (this.reader.getResult()) {
            case SUCCESS: {
                int dataLength = reader.getReadVarInt();
                int bytesRead = reader.getBytesRead();
                if (dataLength < 0) {
                    Log.error("[VarIntFrameDecoder] Bad data length");
                } else if (dataLength == 0) {
                    in.readerIndex(varIntEnd + 1);
                } else {
                    int minimumRead = bytesRead + dataLength;

                    if (in.isReadable(minimumRead)) {
                        if (collectResend) {
                            if (readerIndex0) this.resend.offerLast(in.copy().readerIndex(0));
                            //Log.error("IN BUF COUNT: " + resend.size());
                        }

                        out.add(in.retainedSlice(varIntEnd + 1, dataLength));
                        in.skipBytes(minimumRead);
                    }
                }
            }
            case TOO_BIG: {
                Log.error("[VarIntFrameDecoder] Too big data");
            }
        }
    }*/

    /*static void decodePackets(ByteBuf in, Consumer<ByteBuf> consumer, VarIntByteDecoder reader) {
        while (in.isReadable()) {
            reader.reset();
            int varIntEnd = in.forEachByte(reader);

            if (varIntEnd == -1) {
                // No complete VarInt length header, so exit the loop
                break;
            }

            if (reader.getResult() == VarIntByteDecoder.DecodeResult.SUCCESS) {
                int readVarInt = reader.getReadVarInt();
                int bytesRead = reader.getBytesRead();

                if (readVarInt < 0) {
                    Log.warning("[VarIntFrameDecoder] Bad data length");
                } else if (readVarInt == 0) {
                    in.readerIndex(varIntEnd + 1);
                } else {
                    int minimumRead = bytesRead + readVarInt;

                    if (in.isReadable(minimumRead)) {
                        ByteBuf packetSlice = in.retainedSlice(varIntEnd + 1, readVarInt);
                        consumer.accept(packetSlice);
                        in.skipBytes(minimumRead); // Move to the next packet
                    } else {
                        break; // Wait for more data to arrive
                    }
                }
            } else if (reader.getResult() == VarIntByteDecoder.DecodeResult.TOO_BIG) {
                Log.warning("[VarIntFrameDecoder] Too big data");
                break;
            }
        }
    }*/
}