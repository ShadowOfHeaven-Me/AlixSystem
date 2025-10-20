package alix.common.utils.netty;

import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;

public final class FastNettyUtils {

    //https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/

    //Optimize VarInts with explicit cases
    public static void writeVarInt(ByteBuf buf, int i) {
        if ((i & (0xFFFFFFFF << 7)) == 0) {
            // 1 byte case
            buf.writeByte(i);
        } else if ((i & (0xFFFFFFFF << 14)) == 0) {
            // 2 byte case
            int w = (i & 0x7F | 0x80) << 8 | (i >>> 7);
            buf.writeShort(w);
        } else if ((i & (0xFFFFFFFF << 21)) == 0) {
            // 3 byte case
            int w = ((i & 0x7F | 0x80) << 16) | ((i >>> 7 & 0x7F | 0x80) << 8) | (i >>> 14);
            buf.writeMedium(w);
        } else if ((i & (0xFFFFFFFF << 28)) == 0) {
            // 4 byte case
            int w = ((i & 0x7F | 0x80) << 24) | ((i >>> 7 & 0x7F | 0x80) << 16) | ((i >>> 14 & 0x7F | 0x80) << 8) | (i >>> 21);
            buf.writeInt(w);
        } else {
            // 5 byte case (the max size for a VarInt)
            // Write the first 4 bytes as an int
            int w = ((i & 0x7F | 0x80) << 24) | ((i >>> 7 & 0x7F | 0x80) << 16) | ((i >>> 14 & 0x7F | 0x80) << 8) | (i >>> 21 & 0x7F | 0x80);
            buf.writeInt(w); // Write the first 4 bytes
            buf.writeByte(i >>> 28); // Write the remaining 5th byte
        }
    }

    //Optimize VarInts with explicit cases when their exact size is known
    public static void writeVarInt0(ByteBuf buf, int i, byte bytesCount) {
        switch (bytesCount) {
            case 1:
                buf.writeByte(i);
                return;
            case 2: {
                int w = (i & 0x7F | 0x80) << 8 | (i >>> 7);
                buf.writeShort(w);
                return;
            }
            case 3: {
                int w = ((i & 0x7F | 0x80) << 16) | ((i >>> 7 & 0x7F | 0x80) << 8) | (i >>> 14);
                buf.writeMedium(w);
                return;
            }
            case 4: {
                int w = ((i & 0x7F | 0x80) << 24) | ((i >>> 7 & 0x7F | 0x80) << 16) | ((i >>> 14 & 0x7F | 0x80) << 8) | (i >>> 21);
                buf.writeInt(w);
                return;
            }
            default: {
                int w = ((i & 0x7F | 0x80) << 24) | ((i >>> 7 & 0x7F | 0x80) << 16) | ((i >>> 14 & 0x7F | 0x80) << 8) | (i >>> 21 & 0x7F | 0x80);
                buf.writeInt(w); // Write the first 4 bytes
                buf.writeByte(i >>> 28); // Write the remaining 5th byte
            }
        }
    }

    public static byte countBytesToEncodeVarInt(int i) {
        if ((i & (0xFFFFFFFF << 7)) == 0) return 1;
        if ((i & (0xFFFFFFFF << 14)) == 0) return 2;
        if ((i & (0xFFFFFFFF << 21)) == 0) return 3;
        if ((i & (0xFFFFFFFF << 28)) == 0) return 4;
        return 5;
    }

    //https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/netty/FallbackVarInt21FrameDecoder.java#L69

    public static int readVarInt(ByteBuf buf) {
        switch (buf.readableBytes()) {
            case 3:
                return readVarInt3Or4Byte(buf, buf.getMediumLE(buf.readerIndex()));
            case 2:
                return readVarInt2Byte(buf);
            case 1: {
                byte val = buf.readByte();
                //check if it has the continuation bit set
                if ((val & 0x80) != 0) throw NettySafety.INVALID_VAR_INT;
                return val;
            }
            case 0:
                throw NettySafety.INVALID_VAR_INT;
            //case 3:
            default:
                return readVarInt3Or4Byte(buf, buf.getIntLE(buf.readerIndex()));
        }
    }

    public static int readVarIntSlow(ByteBuf buffer) {
        int i = 0;
        int i1 = 0;

        byte b;
        do {
            b = buffer.readByte();
            i |= (b & 127) << i1++ * 7;
            if (i1 > 5) throw NettySafety.INVALID_VAR_INT;
        } while ((b & 128) == 128);

        return i;
    }

    private static int readVarInt3Or4Byte(final ByteBuf buf, final int wholeOrMore) {
        // Read 3 bytes in little-endian order
        final int atStop = ~wholeOrMore & 0x808080; // Check for stop bits

        // If no stop bits are found, default to a slow read
        // I'm too lazy to make a 5 byte read method
        if (atStop == 0) return readVarIntSlow(buf);

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
        if (atStop == 0) throw new AlixException("2 byte VarInt too big");

        // Find the first stop bit
        final int bitsToKeep = Integer.numberOfTrailingZeros(atStop) + 1;
        buf.skipBytes(bitsToKeep >> 3); // Skip the number of processed bytes

        // Extract and preserve the relevant 7-bit chunks
        int preservedBytes = wholeOrMore & (atStop ^ (atStop - 1));

        // Compact the 7-bit chunks into a single integer
        preservedBytes = (preservedBytes & 0x007F) | ((preservedBytes & 0x7F00) >> 1);
        return preservedBytes;
    }

    public static void init() {
    }

    /*    private static int countBytesToEncodeVarInt(int value) {
        int bytes = 1;
        while ((value & -128) != 0) {//-128 is 0x80 in hex
            bytes++;
            value >>>= 7;
        }
        return bytes;
    }*/

    //Using Paper's VarInt optimizations
/*

/*    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        int maxRead = Math.min(5, buf.readableBytes());

        for (int j = 0; j < maxRead; j++) {
            int k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) return i;
        }

        throw new AlixException("fucked up");
    }*/

/*    private static void writeSlow(ByteBuf buf, int i) {
        // Paper end - Optimize VarInts
        while ((i & -128) != 0) {
            buf.writeByte(i & 127 | 128);
            i >>>= 7;
        }

        buf.writeByte(i);
    }*/

    /*private static final ChannelRemoveHandler removeHandler = ChannelRemoveHandler.createImpl();

    public static void remove(Channel channel, String handlerName) {
        removeHandler.remove(channel, handlerName);
    }*/
}