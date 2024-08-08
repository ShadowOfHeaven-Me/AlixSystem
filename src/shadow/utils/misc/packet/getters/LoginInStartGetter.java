package shadow.utils.misc.packet.getters;

import alix.common.utils.other.annotation.AlixIntrinsified;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public final class LoginInStartGetter {

    //private static final int MAX_LENGTH = 64;//16 * 4

    //it's for both: speed & safety
    @AlixIntrinsified(method = "WrapperLoginClientLoginStart#read")
    public static String getName(ByteBuf buffer) {
        /*if (event.getLastUsedWrapper() != null)
            return ((WrapperLoginClientLoginStart) event.getLastUsedWrapper()).getUsername();*/
        //ByteBuf buffer = (ByteBuf) event.getByteBuf();
        //from PacketWrapper#readString(int)
        int length = readVarInt(buffer);
        //Main.logError("LENGTH: " + length);
        if (length > 64 || length <= 0) return null;//an invalid name length was sent
        String name = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        //Main.logError("NAME: " + name + " LENGTH OF NAME: " + name.length());
        if (name.length() > 16) return null;//an invalid name was sent
        buffer.readerIndex(buffer.readerIndex() + length);
        return name;
    }

    private static int readVarInt(ByteBuf buffer) {
        int value = 0;
        int length = 0;

        byte currentByte;
        do {
            currentByte = buffer.readByte();
            value |= (currentByte & 127) << length * 7;
            if (++length == 6) return -1;//the VarInt is too big, normally this is handled with an error throw, but we can just return -1 here
        } while((currentByte & 128) == 128);

        return value;
    }

    private LoginInStartGetter() {
    }
}