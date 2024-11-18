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

package nanolimbo.alix.connection.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import nanolimbo.alix.server.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public final class VarIntFrameDecoder extends ByteToMessageDecoder {

    //https://github.com/Nan1t/NanoLimbo/blob/main/src/main/java/ua/nanit/limbo/connection/pipeline/VarIntFrameDecoder.java

    //private static final Boolean PRESENT = Boolean.TRUE;
    private boolean collectResend = true;
    //private final Map<ByteBuf, Integer> resendMap = new IdentityHashMap<>(2);
    private final Deque<ByteBuf> resend = new ArrayDeque<>(2);
    private final VarIntByteDecoder reader = new VarIntByteDecoder();

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

    public void forEachCollected(Consumer<ByteBuf> consumer) {
        this.resend.forEach(consumer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        boolean readerIndex0 = in.readerIndex() == 0;
        //Log.error("READER IDX=0: " + readerIndex0);

        this.reader.reset();
        int varIntEnd = in.forEachByte(reader);

        if (varIntEnd == -1) return;

        if (reader.getResult() == VarIntByteDecoder.DecodeResult.SUCCESS) {
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
                        Log.error("IN BUF COUNT: " + resend.size());
                        /*Integer previousWriterIndex = this.resendMap.put(in, in.writerIndex());

                        if (previousWriterIndex == null) {//brand new ByteBuf instance, let's assume it's a new packet
                            this.resend.offerLast(in.copy().readerIndex(0));
                        } *//*else if (previousWriterIndex != in.writerIndex()) {//the ByteBuf's instance is already saved, but something more was written into it
                            this.resend.pop().release();//release
                            this.resend.offerLast(in.copy().readerIndex(0));
                        }*/
                    }

                    out.add(in.retainedSlice(varIntEnd + 1, dataLength));
                    in.skipBytes(minimumRead);
                }
            }
        } else if (reader.getResult() == VarIntByteDecoder.DecodeResult.TOO_BIG) {
            Log.error("[VarIntFrameDecoder] Too big data");
        }
    }

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