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

package nanolimbo.alix.protocol;

import alix.common.utils.netty.FastNettyUtils;
import io.netty.buffer.*;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import nanolimbo.alix.protocol.registry.Version;
import net.kyori.adventure.nbt.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

@SuppressWarnings({"unused", "UnusedReturnValue", "EqualsWhichDoesntCheckParameterClass", "EqualsDoesntCheckParameterClass"})
public final class ByteMessage {

    private final ByteBuf buf;

    public ByteMessage(ByteBuf buf) {
        this.buf = buf;
    }

    public ByteBuf getBuf() {
        return buf;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    /* Minecraft's protocol methods */

    public int readVarInt() {
        int i = 0;
        int maxRead = Math.min(5, buf.readableBytes());

        for (int j = 0; j < maxRead; j++) {
            int k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }

        buf.readBytes(maxRead);

        throw new IllegalArgumentException("Cannot read VarInt");
    }

    public void writeVarInt(int value) {
        FastNettyUtils.writeVarInt(this.buf, value);
    }

    public String readString() {
        return readString(readVarInt());
    }

    public String readString(int length) {
        String str = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.skipBytes(length);
        return str;
    }

    public void writeString(CharSequence str) {
        int size = ByteBufUtil.utf8Bytes(str);
        writeVarInt(size);
        buf.writeCharSequence(str, StandardCharsets.UTF_8);
    }

    public byte[] readBytesArray() {
        int length = readVarInt();
        byte[] array = new byte[length];
        buf.readBytes(array);
        return array;
    }

    public void writeBytesArray(byte[] array) {
        writeVarInt(array.length);
        buf.writeBytes(array);
    }

    public int[] readIntArray() {
        int len = readVarInt();
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = readVarInt();
        }
        return array;
    }

    public UUID readUuid() {
        long msb = buf.readLong();
        long lsb = buf.readLong();
        return new UUID(msb, lsb);
    }

    public void writeUuid(UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public String[] readStringsArray() {
        int length = readVarInt();
        String[] ret = new String[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readString();
        }
        return ret;
    }

    public void writeStringsArray(String[] stringArray) {
        writeVarInt(stringArray.length);
        for (String str : stringArray) {
            writeString(str);
        }
    }

    public void writeVarIntArray(int[] array) {
        writeVarInt(array.length);
        for (int i : array) {
            writeVarInt(i);
        }
    }

    public void writeLongArray(long[] array) {
        writeVarInt(array.length);
        for (long i : array) {
            writeLong(i);
        }
    }

    public void writeCompoundTagArray(CompoundBinaryTag[] compoundTags) {
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buf)) {
            writeVarInt(compoundTags.length);

            for (CompoundBinaryTag tag : compoundTags) {
                BinaryTagIO.writer().write(tag, (OutputStream) stream);
            }
        } catch (IOException e) {
            throw new EncoderException("Cannot write NBT CompoundTag");
        }
    }

    public CompoundBinaryTag readCompoundTag() {
        try (ByteBufInputStream stream = new ByteBufInputStream(buf)) {
            return BinaryTagIO.reader().read((InputStream) stream);
        } catch (IOException thrown) {
            throw new DecoderException("Cannot read NBT CompoundTag");
        }
    }

    public void writeCompoundTag(CompoundBinaryTag compoundTag) {
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buf)) {
            BinaryTagIO.writer().write(compoundTag, (OutputStream) stream);
        } catch (IOException e) {
            throw new EncoderException("Cannot write NBT CompoundTag");
        }
    }

    public void writeNamelessCompoundTag(BinaryTag binaryTag) {
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buf)) {
            stream.writeByte(binaryTag.type().id());

            // TODO Find a way to improve this...
            if (binaryTag instanceof CompoundBinaryTag) {
                CompoundBinaryTag tag = (CompoundBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            } else if (binaryTag instanceof ByteBinaryTag) {
                ByteBinaryTag tag = (ByteBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            } else if (binaryTag instanceof ShortBinaryTag) {
                ShortBinaryTag tag = (ShortBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            } else if (binaryTag instanceof IntBinaryTag) {
                IntBinaryTag tag = (IntBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            } else if (binaryTag instanceof LongBinaryTag) {
                LongBinaryTag tag = (LongBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            } else if (binaryTag instanceof DoubleBinaryTag) {
                DoubleBinaryTag tag = (DoubleBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            } else if (binaryTag instanceof StringBinaryTag) {
                StringBinaryTag tag = (StringBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            } else if (binaryTag instanceof ListBinaryTag) {
                ListBinaryTag tag = (ListBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            } else if (binaryTag instanceof EndBinaryTag) {
                EndBinaryTag tag = (EndBinaryTag) binaryTag;
                tag.type().write(tag, stream);
            }

        } catch (IOException e) {
            throw new EncoderException("Cannot write NBT CompoundTag");
        }
    }

    public void writeNbtMessage(NbtMessage nbtMessage, Version version) {
        if (version.moreOrEqual(Version.V1_20_3)) {
            writeNamelessCompoundTag(nbtMessage.getTag());
        } else {
            writeString(nbtMessage.getJson());
        }
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumset, Class<E> oclass) {
        E[] enums = oclass.getEnumConstants();
        BitSet bits = new BitSet(enums.length);

        for (int i = 0; i < enums.length; ++i) {
            bits.set(i, enumset.contains(enums[i]));
        }

        writeFixedBitSet(bits, enums.length, buf);
    }

    private static void writeFixedBitSet(BitSet bits, int size, ByteBuf buf) {
        if (bits.length() > size) {
            throw new StackOverflowError("BitSet too large (expected " + size + " got " + bits.size() + ")");
        }
        buf.writeBytes(Arrays.copyOf(bits.toByteArray(), (size + 8) >> 3));
    }

    /* Delegated methods */

    public int capacity() {
        return buf.capacity();
    }

    public ByteBuf capacity(int newCapacity) {
        return buf.capacity(newCapacity);
    }

    public int maxCapacity() {
        return buf.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return buf.alloc();
    }

    @Deprecated
    public ByteOrder order() {
        return buf.order();
    }

    @Deprecated
    public ByteBuf order(ByteOrder endianness) {
        return buf.order(endianness);
    }

    public ByteBuf unwrap() {
        return buf.unwrap();
    }

    public boolean isDirect() {
        return buf.isDirect();
    }

    public boolean isReadOnly() {
        return buf.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return buf.asReadOnly();
    }

    public int readerIndex() {
        return buf.readerIndex();
    }

    public ByteBuf readerIndex(int readerIndex) {
        return buf.readerIndex(readerIndex);
    }

    public int writerIndex() {
        return buf.writerIndex();
    }

    public ByteBuf writerIndex(int writerIndex) {
        return buf.writerIndex(writerIndex);
    }

    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return buf.setIndex(readerIndex, writerIndex);
    }

    public int readableBytes() {
        return buf.readableBytes();
    }

    public int writableBytes() {
        return buf.writableBytes();
    }

    public int maxWritableBytes() {
        return buf.maxWritableBytes();
    }

    public boolean isReadable() {
        return buf.isReadable();
    }

    public boolean isReadable(int size) {
        return buf.isReadable(size);
    }

    public boolean isWritable() {
        return buf.isWritable();
    }

    public boolean isWritable(int size) {
        return buf.isWritable(size);
    }

    public ByteBuf clear() {
        return buf.clear();
    }

    public ByteBuf markReaderIndex() {
        return buf.markReaderIndex();
    }

    public ByteBuf resetReaderIndex() {
        return buf.resetReaderIndex();
    }

    public ByteBuf markWriterIndex() {
        return buf.markWriterIndex();
    }

    public ByteBuf resetWriterIndex() {
        return buf.resetWriterIndex();
    }

    public ByteBuf discardReadBytes() {
        return buf.discardReadBytes();
    }

    public ByteBuf discardSomeReadBytes() {
        return buf.discardSomeReadBytes();
    }

    public ByteBuf ensureWritable(int minWritableBytes) {
        return buf.ensureWritable(minWritableBytes);
    }

    public int ensureWritable(int minWritableBytes, boolean force) {
        return buf.ensureWritable(minWritableBytes, force);
    }

    public boolean getBoolean(int index) {
        return buf.getBoolean(index);
    }

    public byte getByte(int index) {
        return buf.getByte(index);
    }

    public short getUnsignedByte(int index) {
        return buf.getUnsignedByte(index);
    }

    public short getShort(int index) {
        return buf.getShort(index);
    }

    public short getShortLE(int index) {
        return buf.getShortLE(index);
    }

    public int getUnsignedShort(int index) {
        return buf.getUnsignedShort(index);
    }

    public int getUnsignedShortLE(int index) {
        return buf.getUnsignedShortLE(index);
    }

    public int getMedium(int index) {
        return buf.getMedium(index);
    }

    public int getMediumLE(int index) {
        return buf.getMediumLE(index);
    }

    public int getUnsignedMedium(int index) {
        return buf.getUnsignedMedium(index);
    }

    public int getUnsignedMediumLE(int index) {
        return buf.getUnsignedMediumLE(index);
    }

    public int getInt(int index) {
        return buf.getInt(index);
    }

    public int getIntLE(int index) {
        return buf.getIntLE(index);
    }

    public long getUnsignedInt(int index) {
        return buf.getUnsignedInt(index);
    }

    public long getUnsignedIntLE(int index) {
        return buf.getUnsignedIntLE(index);
    }

    public long getLong(int index) {
        return buf.getLong(index);
    }

    public long getLongLE(int index) {
        return buf.getLongLE(index);
    }

    public char getChar(int index) {
        return buf.getChar(index);
    }

    public float getFloat(int index) {
        return buf.getFloat(index);
    }

    public float getFloatLE(int index) {
        return buf.getFloatLE(index);
    }

    public double getDouble(int index) {
        return buf.getDouble(index);
    }

    public double getDoubleLE(int index) {
        return buf.getDoubleLE(index);
    }

    public ByteBuf getBytes(int index, ByteBuf dst) {
        return buf.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return buf.getBytes(index, dst, length);
    }

    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return buf.getBytes(index, dst, dstIndex, length);
    }

    public ByteBuf getBytes(int index, byte[] dst) {
        return buf.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return buf.getBytes(index, dst, dstIndex, length);
    }

    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return buf.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return buf.getBytes(index, out, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return buf.getBytes(index, out, length);
    }

    public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return buf.getBytes(index, out, position, length);
    }

    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return buf.getCharSequence(index, length, charset);
    }

    public ByteBuf setBoolean(int index, boolean value) {
        return buf.setBoolean(index, value);
    }

    public ByteBuf setByte(int index, int value) {
        return buf.setByte(index, value);
    }

    public ByteBuf setShort(int index, int value) {
        return buf.setShort(index, value);
    }

    public ByteBuf setShortLE(int index, int value) {
        return buf.setShortLE(index, value);
    }

    public ByteBuf setMedium(int index, int value) {
        return buf.setMedium(index, value);
    }

    public ByteBuf setMediumLE(int index, int value) {
        return buf.setMediumLE(index, value);
    }

    public ByteBuf setInt(int index, int value) {
        return buf.setInt(index, value);
    }

    public ByteBuf setIntLE(int index, int value) {
        return buf.setIntLE(index, value);
    }

    public ByteBuf setLong(int index, long value) {
        return buf.setLong(index, value);
    }

    public ByteBuf setLongLE(int index, long value) {
        return buf.setLongLE(index, value);
    }

    public ByteBuf setChar(int index, int value) {
        return buf.setChar(index, value);
    }

    public ByteBuf setFloat(int index, float value) {
        return buf.setFloat(index, value);
    }

    public ByteBuf setFloatLE(int index, float value) {
        return buf.setFloatLE(index, value);
    }

    public ByteBuf setDouble(int index, double value) {
        return buf.setDouble(index, value);
    }

    public ByteBuf setDoubleLE(int index, double value) {
        return buf.setDoubleLE(index, value);
    }

    public ByteBuf setBytes(int index, ByteBuf src) {
        return buf.setBytes(index, src);
    }

    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return buf.setBytes(index, src, length);
    }

    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return buf.setBytes(index, src, srcIndex, length);
    }

    public ByteBuf setBytes(int index, byte[] src) {
        return buf.setBytes(index, src);
    }

    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return buf.setBytes(index, src, srcIndex, length);
    }

    public ByteBuf setBytes(int index, ByteBuffer src) {
        return buf.setBytes(index, src);
    }

    public int setBytes(int index, InputStream in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        return buf.setBytes(index, in, position, length);
    }

    public ByteBuf setZero(int index, int length) {
        return buf.setZero(index, length);
    }

    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return buf.setCharSequence(index, sequence, charset);
    }

    public boolean readBoolean() {
        return buf.readBoolean();
    }

    public byte readByte() {
        return buf.readByte();
    }

    public short readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    public short readShort() {
        return buf.readShort();
    }

    public short readShortLE() {
        return buf.readShortLE();
    }

    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return buf.readUnsignedShortLE();
    }

    public int readMedium() {
        return buf.readMedium();
    }

    public int readMediumLE() {
        return buf.readMediumLE();
    }

    public int readUnsignedMedium() {
        return buf.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return buf.readUnsignedMediumLE();
    }

    public int readInt() {
        return buf.readInt();
    }

    public int readIntLE() {
        return buf.readIntLE();
    }

    public long readUnsignedInt() {
        return buf.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return buf.readUnsignedIntLE();
    }

    public long readLong() {
        return buf.readLong();
    }

    public long readLongLE() {
        return buf.readLongLE();
    }

    public char readChar() {
        return buf.readChar();
    }

    public float readFloat() {
        return buf.readFloat();
    }

    public float readFloatLE() {
        return buf.readFloatLE();
    }

    public double readDouble() {
        return buf.readDouble();
    }

    public double readDoubleLE() {
        return buf.readDoubleLE();
    }

    public ByteBuf readBytes(int length) {
        return buf.readBytes(length);
    }

    public ByteBuf readSlice(int length) {
        return buf.readSlice(length);
    }

    public ByteBuf readRetainedSlice(int length) {
        return buf.readRetainedSlice(length);
    }

    public ByteBuf readBytes(ByteBuf dst) {
        return buf.readBytes(dst);
    }

    public ByteBuf readBytes(ByteBuf dst, int length) {
        return buf.readBytes(dst, length);
    }

    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return buf.readBytes(dst, dstIndex, length);
    }

    public ByteBuf readBytes(byte[] dst) {
        return buf.readBytes(dst);
    }

    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return buf.readBytes(dst, dstIndex, length);
    }

    public ByteBuf readBytes(ByteBuffer dst) {
        return buf.readBytes(dst);
    }

    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return buf.readBytes(out, length);
    }

    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return buf.readBytes(out, length);
    }

    public CharSequence readCharSequence(int length, Charset charset) {
        return buf.readCharSequence(length, charset);
    }

    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return buf.readBytes(out, position, length);
    }

    public ByteBuf skipBytes(int length) {
        return buf.skipBytes(length);
    }

    public ByteBuf writeBoolean(boolean value) {
        return buf.writeBoolean(value);
    }

    public ByteBuf writeByte(int value) {
        return buf.writeByte(value);
    }

    public ByteBuf writeShort(int value) {
        return buf.writeShort(value);
    }

    public ByteBuf writeShortLE(int value) {
        return buf.writeShortLE(value);
    }

    public ByteBuf writeMedium(int value) {
        return buf.writeMedium(value);
    }

    public ByteBuf writeMediumLE(int value) {
        return buf.writeMediumLE(value);
    }

    public ByteBuf writeInt(int value) {
        return buf.writeInt(value);
    }

    public ByteBuf writeIntLE(int value) {
        return buf.writeIntLE(value);
    }

    public ByteBuf writeLong(long value) {
        return buf.writeLong(value);
    }

    public ByteBuf writeLongLE(long value) {
        return buf.writeLongLE(value);
    }

    public ByteBuf writeChar(int value) {
        return buf.writeChar(value);
    }

    public ByteBuf writeFloat(float value) {
        return buf.writeFloat(value);
    }

    public ByteBuf writeFloatLE(float value) {
        return buf.writeFloatLE(value);
    }

    public ByteBuf writeDouble(double value) {
        return buf.writeDouble(value);
    }

    public ByteBuf writeDoubleLE(double value) {
        return buf.writeDoubleLE(value);
    }

    public ByteBuf writeBytes(ByteBuf src) {
        return buf.writeBytes(src);
    }

    public ByteBuf writeBytes(ByteBuf src, int length) {
        return buf.writeBytes(src, length);
    }

    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return buf.writeBytes(src, srcIndex, length);
    }

    public ByteBuf writeBytes(byte[] src) {
        return buf.writeBytes(src);
    }

    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return buf.writeBytes(src, srcIndex, length);
    }

    public ByteBuf writeBytes(ByteBuffer src) {
        return buf.writeBytes(src);
    }

    public int writeBytes(InputStream in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    public int writeBytes(FileChannel in, long position, int length) throws IOException {
        return buf.writeBytes(in, position, length);
    }

    public ByteBuf writeZero(int length) {
        return buf.writeZero(length);
    }

    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return buf.writeCharSequence(sequence, charset);
    }

    public int indexOf(int fromIndex, int toIndex, byte value) {
        return buf.indexOf(fromIndex, toIndex, value);
    }

    public int bytesBefore(byte value) {
        return buf.bytesBefore(value);
    }

    public int bytesBefore(int length, byte value) {
        return buf.bytesBefore(length, value);
    }

    public int bytesBefore(int index, int length, byte value) {
        return buf.bytesBefore(index, length, value);
    }

    public int forEachByte(ByteProcessor processor) {
        return buf.forEachByte(processor);
    }

    public int forEachByte(int index, int length, ByteProcessor processor) {
        return buf.forEachByte(index, length, processor);
    }

    public int forEachByteDesc(ByteProcessor processor) {
        return buf.forEachByteDesc(processor);
    }

    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return buf.forEachByteDesc(index, length, processor);
    }

    public ByteBuf copy() {
        return buf.copy();
    }

    public ByteBuf copy(int index, int length) {
        return buf.copy(index, length);
    }

    public ByteBuf slice() {
        return buf.slice();
    }

    public ByteBuf retainedSlice() {
        return buf.retainedSlice();
    }

    public ByteBuf slice(int index, int length) {
        return buf.slice(index, length);
    }

    public ByteBuf retainedSlice(int index, int length) {
        return buf.retainedSlice(index, length);
    }

    public ByteBuf duplicate() {
        return buf.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return buf.retainedDuplicate();
    }

    public int nioBufferCount() {
        return buf.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return buf.nioBuffer();
    }

    public ByteBuffer nioBuffer(int index, int length) {
        return buf.nioBuffer(index, length);
    }

    public ByteBuffer internalNioBuffer(int index, int length) {
        return buf.internalNioBuffer(index, length);
    }

    public ByteBuffer[] nioBuffers() {
        return buf.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int index, int length) {
        return buf.nioBuffers(index, length);
    }

    public boolean hasArray() {
        return buf.hasArray();
    }

    public byte[] array() {
        return buf.array();
    }

    public int arrayOffset() {
        return buf.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return buf.hasMemoryAddress();
    }

    public long memoryAddress() {
        return buf.memoryAddress();
    }

    public String toString(Charset charset) {
        return buf.toString(charset);
    }

    public String toString(int index, int length, Charset charset) {
        return buf.toString(index, length, charset);
    }

    public int hashCode() {
        return buf.hashCode();
    }

    public boolean equals(Object obj) {
        return buf.equals(obj);
    }

    public int compareTo(ByteBuf buffer) {
        return buf.compareTo(buffer);
    }

    public String toString() {
        return buf.toString();
    }

    public ByteBuf retain(int increment) {
        return buf.retain(increment);
    }

    public ByteBuf retain() {
        return buf.retain();
    }

    public ByteBuf touch() {
        return buf.touch();
    }

    public ByteBuf touch(Object hint) {
        return buf.touch(hint);
    }

    public int refCnt() {
        return buf.refCnt();
    }

    public boolean release() {
        return buf.release();
    }

    public boolean release(int decrement) {
        return buf.release(decrement);
    }

    public static ByteMessage create() {
        return new ByteMessage(Unpooled.buffer());
    }
}
