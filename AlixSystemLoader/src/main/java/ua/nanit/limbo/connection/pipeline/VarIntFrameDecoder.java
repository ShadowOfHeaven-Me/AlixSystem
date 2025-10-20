/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo.connection.pipeline;

import alix.common.utils.netty.BufUtils;
import alix.common.utils.netty.NettySafety;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder.Cumulator;
import io.netty.util.ByteProcessor;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.server.Log;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

import static io.netty.handler.codec.ByteToMessageDecoder.MERGE_CUMULATOR;

public final class VarIntFrameDecoder extends ChannelInboundHandlerAdapter {

    //Original: https://github.com/Nan1t/NanoLimbo/blob/main/src/main/java/ua/nanit/limbo/connection/pipeline/VarIntFrameDecoder.java
    //Optimized with: https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/netty/FallbackVarInt21FrameDecoder.java#L69

    //private static final Boolean PRESENT = Boolean.TRUE;
    private boolean collectResend = true;
    private CipherHandler cipher;
    //private final Map<ByteBuf, Integer> resendMap = new IdentityHashMap<>(2);
    //private final FixedSizeQueue<ByteBuf> collectedIdentity = new FixedSizeQueue<>(2);
    //private final FixedSizeQueue<ByteBuf> resendCopies = new FixedSizeQueue<>(2);
    //private final Deque<ByteBuf> resend = new ArrayDeque<>(2);

    //must be an IdentityHashMap, cuz the ByteBuf#hashCode changes depending on its readerIndex
    private final Set<ByteBuf> resend = Collections.newSetFromMap(new IdentityHashMap<>(2));
    private final ClientConnection connection;

    public VarIntFrameDecoder(ClientConnection connection) {
        this.connection = connection;
    }
    //private final BufSet12 resend = new BufSet12();

    //private final VarIntByteDecoder reader = new VarIntByteDecoder();

    public void stopResendCollection() {
        this.collectResend = false;
    }

    public void releaseCollected() {
        this.forEachCollected(ByteBuf::release);
        /*this.forEachCollected(buf -> {
            int refCnt = buf.refCnt();
            if (refCnt != 0) buf.release(refCnt);
        });*/
    }

    /*public void resendCollected(Channel channel) {
        this.forEachCollected(buf -> channel.pipeline().fireChannelRead(buf));
    }*/

    public void resendCollected(Channel channel) {
        /*this.forEachCollected(buf ->
                channel.pipeline().fireChannelRead(buf.readerIndex(0)));*/
        //readerIndex is already set by BufSet12
        this.forEachCollected(buf -> channel.pipeline().fireChannelRead(buf.readerIndex(0)));
    }

    private void forEachCollected(Consumer<ByteBuf> consumer) {
        this.resend.forEach(consumer);
    }

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
            if (this.cumulation != null)
                e.execute(this::cleanUp);
        }
    }

    private void cleanUp() {
        /*if (!this.connection.getChannel().eventLoop().inEventLoop())
            throw new AlixError("AAAAAAA " + Thread.currentThread());*/

        ByteBuf cum = this.cumulation;
        if (cum == null) return;

        this.cumulation = null;
        //super.channelRead(ctx, this.cumulation);

        //will always be readable
        /*if (cum.isReadable())
            Log.error("CUM SIZE= " + cum.readableBytes());*/

        //safe release
        //if (cum.refCnt() != 0)
        //(no need now, since executed on event loop)
        cum.release();
    }

    private final Cumulator cumulator = MERGE_CUMULATOR;
    private ByteBuf cumulation;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //two volatile reads should be good enough perf here, since arriving packets should be large enough to overshadow this minor perf dent
        if (!ctx.channel().isActive() || ctx.isRemoved()) {
            this.cleanUp();
            return;
        }

        if (NanoLimbo.debugFrames)
            Log.error("FRAME=" + msg);

        ByteBuf in = this.tryDecrypt((ByteBuf) msg);

        if (NanoLimbo.debugBytes)
            PacketUtils.debugBytes(in);

        //From ByteToMessageDecoder
        if (this.cumulation == null) this.cumulation = in;
        else {
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
            this.cumulation.release();
            this.cumulation = null;
        } else if (this.cumulation.readerIndex() > 1024) {
            //is discardSomeReadBytes a better alternative here?
            this.cumulation.discardReadBytes();
        }
    }

    //@Override
    private ByteBuf decode(ByteBuf in) {
        int packetStart = in.forEachByte(ByteProcessor.FIND_NON_NUL);
        if (packetStart == -1) {
            in.clear();
            //in.readerIndex(0);
            return null;
        }
        //the name of the var isn't exactly true ;]
        //pretty sure it returns true, like, always
        //not sure why
        //boolean notReadYet = in.readerIndex() == packetStart;
        in.readerIndex(packetStart);
        in.markReaderIndex();

        int readableBytes = in.readableBytes();
        if (readableBytes == 0) return null;//I don't think this can ever be true here

        int len = readVarIntPacketLength(in);
        //NettySafety.validateUserInputBufAlloc(len);
        if (len < 0) throw NettySafety.INVALID_PACKET_LEN;

        //readVarInt() returns 0 for partial VarInts with a continuation bit, and for actual VarInts read as a 0
        if (len == 0) {
            in.resetReaderIndex();
            return null;
        }

        //the packet is said to be larger than what we've cumulated (hehe) so far
        if (len > readableBytes) {
            in.resetReaderIndex();
            return null;
        }
        //Log.error("PACKET START: " + packetStart + " IDX: " + in.readerIndex() + " notReadYet: " + notReadYet);

        if (collectResend && !this.resend.contains(in))// && notReadYet) {
            this.resend.add(in.retain());
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
    /*private static final class BufSet12 {

        //Inspired by java's ImmutableCollections.List12 from List.of(e1, e2)

        private ByteBuf buf1;
        private ByteBuf buf2;

        //HAProxyDecoder could've changed the reader index

        //private int rIdx1;
        //private int rIdx2;

        private BufSet12() {
        }

        private void add(ByteBuf buf) {
            if (this.buf1 == null) {
                this.buf1 = buf;
                //this.rIdx1 = buf.readerIndex();
                return;
            }
            if (this.buf1 == buf) return;
            this.buf2 = buf;
            //this.rIdx2 = buf.readerIndex();
        }

        private void forEach(Consumer<ByteBuf> consumer) {
            if (this.buf1 == null) return;

            //this.buf1.readerIndex(this.rIdx1);
            consumer.accept(this.buf1);

            if (this.buf2 == null) return;

            //this.buf2.readerIndex(this.rIdx2);
            consumer.accept(this.buf2);
        }
    }*/

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