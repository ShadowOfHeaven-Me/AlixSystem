package shadow.utils.holders.packet.constructors;

import shadow.utils.holders.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class OutHeldItemSlotPacketConstructor {

    private static final Constructor<?> constructor;

    static {
        Class<?> packetClazz = ReflectionUtils.outHeldItemSlotPacketClass;
        constructor = ReflectionUtils.getConstructor(packetClazz, int.class);
    }

    public static Object construct(int slot) {
        try {
            return constructor.newInstance(slot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}