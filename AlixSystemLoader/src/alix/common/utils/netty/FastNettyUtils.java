package alix.common.utils.netty;

import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;

public final class FastNettyUtils {

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

    //Optimize VarInts with explicit cases when its exact size is known
    public static void writeVarInt(ByteBuf buf, int i, byte bytesCount) {
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

    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        int maxRead = Math.min(5, buf.readableBytes());

        for (int j = 0; j < maxRead; j++) {
            int k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) return i;
        }

        throw new AlixException("fucked up");
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
/*    public static void writeVarInt(ByteBuf buf, int i) {
        // Paper start - Optimize VarInts
        // Peel the one and two byte count cases explicitly as they are the most common VarInt sizes
        // that the proxy will write, to improve inlining.
        if ((i & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(i);
        } else if ((i & (0xFFFFFFFF << 14)) == 0) {
            int w = (i & 0x7F | 0x80) << 8 | (i >>> 7);
            buf.writeShort(w);
        } else writeSlow(buf, i);
    }

    private static void writeSlow(ByteBuf buf, int i) {
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