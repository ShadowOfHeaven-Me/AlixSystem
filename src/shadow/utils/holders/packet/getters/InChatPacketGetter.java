package shadow.utils.holders.packet.getters;

import alix.common.utils.other.throwable.AlixException;
import shadow.utils.holders.ReflectionUtils;

import java.lang.reflect.Method;

public final class InChatPacketGetter {

    private static final Method getMsg;

    static {
        Class<?> clazz = ReflectionUtils.nms2("network.protocol.game.PacketPlayInChat", "network.protocol.game.ServerboundChatPacket");
        getMsg = ReflectionUtils.getStringMethodFromPacketClass(clazz);
    }

    public static String getMessage(Object packet) {
        try {
            return (String) getMsg.invoke(packet);
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }
}