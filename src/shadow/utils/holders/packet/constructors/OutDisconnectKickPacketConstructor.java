package shadow.utils.holders.packet.constructors;

import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.file.FileManager;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public final class OutDisconnectKickPacketConstructor {

    //private static final Constructor<?> IChatBaseComponentConstructor;
    private static final Constructor<?> playPhasePacketConstructor, loginPhasePacketConstructor;

    private static Constructor<?> initCons0(Class<?> packetClazz) {
        Constructor<?> cons = null;

        for (Constructor<?> c : packetClazz.getConstructors()) {
            if (c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom(ReflectionUtils.chatComponentTextClass)) {
                cons = c;
                break;
            }
        }
        if (cons == null)
            throw new RuntimeException("Not found: " + Arrays.toString(packetClazz.getConstructors()) + " " + ReflectionUtils.chatComponentTextClass);
        return cons;
    }

    static {
        try {
            //IChatBaseComponentConstructor = ReflectionUtils.chatComponentTextClass.getConstructor(String.class);
            //Class<?> packetClazz = ReflectionUtils.disconnectKickPacketClass;
            playPhasePacketConstructor = initCons0(ReflectionUtils.disconnectKickPlayPhasePacketClass);
            loginPhasePacketConstructor = initCons0(ReflectionUtils.disconnectLoginPhasePacketClass);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Object constructAtLoginPhase(String message) {
        try {
            Object chatComponent = ((Object[]) ReflectionUtils.IChatComponentArrayFromStringMethod.invoke(null, message))[0];
            return loginPhasePacketConstructor.newInstance(chatComponent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object constructAtPlayPhase(String message) {
        try {
            Object chatComponent = ((Object[]) ReflectionUtils.IChatComponentArrayFromStringMethod.invoke(null, message))[0];
            return playPhasePacketConstructor.newInstance(chatComponent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OutDisconnectKickPacketConstructor() {
    }
}