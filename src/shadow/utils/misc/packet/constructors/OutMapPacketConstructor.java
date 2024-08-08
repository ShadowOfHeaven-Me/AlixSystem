package shadow.utils.misc.packet.constructors;

import io.netty.buffer.ByteBuf;
import shadow.utils.misc.packet.custom.AlixMapDataPacketWrapper;

public final class OutMapPacketConstructor {

/*    private static final Class<?> packetClass = ReflectionUtils.outMapPacketClass;
    private static final Constructor<?> packetClassConstructor;

    private static final Class<?> packetDimensionClass;
    private static final Constructor<?> packetDimensionClassConstructor;
    private static final boolean newerConstructor;
    private static final byte MAP_SCALE = 3;

    static {
        Class<?> dimensionClazz = null;

        for (Constructor<?> constructor : packetClass.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length > 1 && parameterTypes[0] == int.class) {//length > 1 to ensure it isn't the PacketDataSerializer constructor
                Class<?> lastParam = parameterTypes[parameterTypes.length - 1];
                if (!lastParam.isPrimitive()) {
                    dimensionClazz = lastParam;
                    break;
                }
            }
        }

        packetDimensionClass = dimensionClazz;
        newerConstructor = dimensionClazz != null;

        if (newerConstructor) {
            packetClassConstructor = ReflectionUtils.getConstructor(packetClass, int.class, byte.class, boolean.class, Collection.class, packetDimensionClass);
            packetDimensionClassConstructor = ReflectionUtils.getConstructor(packetDimensionClass, int.class, int.class, int.class, int.class, byte[].class);
        } else {
            packetClassConstructor = ReflectionUtils.getConstructor(packetClass, int.class, byte.class, boolean.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
            packetDimensionClassConstructor = null;
        }
    }*/




    public static ByteBuf constructDynamic(int viewId, byte[] toDrawBytes) {
        return AlixMapDataPacketWrapper.createBuffer(viewId, toDrawBytes);
        //return newerConstructor ? construct_1_17(viewId, toDrawBytes) : construct_old(viewId, toDrawBytes);
    }

/*    private static Object construct_1_17(int viewId, byte[] toDrawBytes) {
        try {
            Object dimensionObj = packetDimensionClassConstructor.newInstance(0, 0, 128, 128, toDrawBytes);

            return packetClassConstructor.newInstance(viewId, MAP_SCALE, false, Collections.EMPTY_LIST, dimensionObj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object construct_old(int viewId, byte[] toDrawBytes) {
        try {
            return packetClassConstructor.newInstance(viewId, MAP_SCALE, false, false, Collections.EMPTY_LIST, toDrawBytes, 0, 0, 128, 128);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    private OutMapPacketConstructor() {
    }
}