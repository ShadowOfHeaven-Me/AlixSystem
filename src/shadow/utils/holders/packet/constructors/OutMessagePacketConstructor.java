package shadow.utils.holders.packet.constructors;

import alix.common.utils.other.throwable.AlixException;
import net.md_5.bungee.api.ChatMessageType;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import shadow.utils.holders.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.UUID;

public final class OutMessagePacketConstructor {

    private static final Constructor<?> constructor;
    private static final boolean newerConstructor;
    private static final Enum<?> SYSTEM_MESSAGE_TYPE, ACTION_BAR;

    static {
        Class<?> clazz = ReflectionUtils.outChatMessagePacketClass;
        boolean newerCons = clazz.getSimpleName().equals("ClientboundSystemChatPacket");//PacketPlayOutChat is the older one
        Constructor<?> cons = null;
        for (Constructor<?> c : clazz.getConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if (newerCons && params.length == 2 && params[0] == ReflectionUtils.IChatBaseComponentClass && params[1] == boolean.class
                    || !newerCons && params.length == 3 && params[0] == ReflectionUtils.IChatBaseComponentClass && params[1] == ReflectionUtils.chatMessageType
                    && params[2] == UUID.class) {
                cons = c;
                break;
            }
        }
        constructor = cons;
        newerConstructor = newerCons;
        SYSTEM_MESSAGE_TYPE = newerCons ? null : (Enum<?>) ReflectionUtils.chatMessageType.getEnumConstants()[1];
        ACTION_BAR = newerCons ? null : (Enum<?>) ReflectionUtils.chatMessageType.getEnumConstants()[2];
    }

    public static Object construct(String message) {
        return construct(message, false);
    }

    public static Object construct(String message, boolean actionBar) {
        try {
            return newerConstructor ? construct_1_19(message, actionBar) : construct_old(message, actionBar);
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    private static Object construct_1_19(String message, boolean actionBar) throws Exception {
        return constructor.newInstance(ReflectionUtils.constructTextComponents(message)[0], actionBar);
    }

    private static Object construct_old(String message, boolean actionBar) throws Exception {
        return constructor.newInstance(ReflectionUtils.constructTextComponents(message)[0], actionBar ? ACTION_BAR : SYSTEM_MESSAGE_TYPE, null);
    }

    private OutMessagePacketConstructor() {
    }
}