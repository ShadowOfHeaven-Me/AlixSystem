package shadow.utils.holders.packet.constructors;

import shadow.utils.holders.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;

public final class OutPlayerInfoPacketConstructor {

    private static final Object ADD_PLAYER;
    private static final Constructor<?> constructor;
    //private static final boolean enumSetConstructor;

    static {
        Class<?> packetClass = ReflectionUtils.outPlayerInfoPacketClass;
        Class<? extends Enum> playerInfoAction = null;
        for (Class<?> clazz : packetClass.getClasses()) {
            if (clazz.isEnum()) {
                playerInfoAction = (Class<? extends Enum>) clazz;
                break;
            }
        }
        //Main.logError(Arrays.toString(packetClass.getConstructors()) + " ");
        Object ADD_PLAYER0 = playerInfoAction.getEnumConstants()[0];//it's an enum constant
        Constructor<?> constructor0;
        try {
            constructor0 = ReflectionUtils.getConstructor(packetClass, playerInfoAction, Collection.class);
        } catch (Throwable e) {

            try {
                constructor0 = ReflectionUtils.getConstructor(packetClass, playerInfoAction, Iterable.class);
            } catch (Throwable ex) {
                constructor0 = ReflectionUtils.getConstructor(packetClass, EnumSet.class, Collection.class);
                Enum UPDATE_LIST = playerInfoAction.getEnumConstants()[3];
                Enum UPDATE_DISPLAY_NAME = playerInfoAction.getEnumConstants()[5];
                //ClientboundPlayerInfoUpdatePacket
                ADD_PLAYER0 = EnumSet.of((Enum) ADD_PLAYER0, UPDATE_LIST, UPDATE_DISPLAY_NAME);//it's overriden to be an EnumSet
            }
        }
        constructor = constructor0;
        ADD_PLAYER = ADD_PLAYER0;
    }

    public static Object constructADD(Collection<Object> nmsPlayers) {
        try {
            return constructor.newInstance(ADD_PLAYER, nmsPlayers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

/*    private static Object construct_ADD_ENUM_SET(Collection<Object> nmsPlayers) {
        try {
            return constructor.newInstance(ADD_PLAYER, nmsPlayers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object construct_ADD_PRE_1_19_3(Collection<Object> nmsPlayers) {
        try {
            return constructor.newInstance(ADD_PLAYER, nmsPlayers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/


    public static void init() {
    }

    private OutPlayerInfoPacketConstructor() {
    }

}