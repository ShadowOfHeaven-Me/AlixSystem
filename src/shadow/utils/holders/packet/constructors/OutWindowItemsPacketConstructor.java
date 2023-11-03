package shadow.utils.holders.packet.constructors;


import org.bukkit.inventory.ItemStack;
import shadow.Main;
import shadow.utils.holders.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutWindowItemsPacketConstructor {

    private static final boolean newerConstructor;
    private static final Constructor<?> clazzConstructor, listConstructor;
    private static final Object nullNmsItemStack;

    static {
        Class<?> packetClazz = ReflectionUtils.outWindowItemsPacketClass;
        Class<?> listClazz = ReflectionUtils.nonNullListClass;
        listConstructor = listClazz.getDeclaredConstructors()[0];
        listConstructor.setAccessible(true);

        boolean newerConstructor0 = false;
        Constructor<?> clazzConstructor0 = null;

        Class<?> nmsItemStackClazz = ReflectionUtils.nmsItemStackClass;
        Object nullNmsItemStack0 = ReflectionUtils.fieldGet(ReflectionUtils.getFieldFromTypeSafe(nmsItemStackClazz, nmsItemStackClazz), null);


        for (Constructor<?> constructor : packetClazz.getConstructors()) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length > 1) {
                if (params[0] == int.class && params[1] == int.class && params[2] == listClazz) {

                    clazzConstructor0 = constructor;
                    newerConstructor0 = true;
                    break;
                } else if (params[0] == int.class && params[1] == listClazz) {
                    clazzConstructor0 = constructor;
                    newerConstructor0 = false;
                    break;
                }
            }
        }
        if (clazzConstructor0 == null)
            throw new ExceptionInInitializerError("Constructors: " + Arrays.toString(packetClazz.getConstructors()));
        nullNmsItemStack = nullNmsItemStack0;
        newerConstructor = newerConstructor0;
        clazzConstructor = clazzConstructor0;
    }

    public static void init() {

    }

    public static Object construct(int windowId, int size, List<ItemStack> list) {
        return construct0(windowId, size, createNMSItemList(list));
    }

    public static Object construct0(int windowId, int size, Object nmsList) {
        return newerConstructor ? construct_1_17(windowId, size, nmsList) : construct_old(windowId, nmsList);
    }

    public static Object construct_1_17(int windowId, int size, Object nmsList) {
        try {
            //Class<?>[] params = clazzConstructor.getParameterTypes();
            //Main.logError(params[0] + " " + params[1] + " " + params[2] + " " + params[3]);
            //Main.logError(int.class + " " + int.class + " " + nmsList.getClass() + " " + nullNmsItemStack.getClass());
            return clazzConstructor.newInstance(windowId, size, nmsList, nullNmsItemStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object construct_old(int windowId, Object nmsList) {
        try {
            return clazzConstructor.newInstance(windowId, nmsList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object createNMSItemList(List<ItemStack> spigotItemList) {
        List<Object> nmsItemList = new ArrayList<>(spigotItemList.size());
        try {

            for (ItemStack itemStack : spigotItemList) nmsItemList.add(itemToNMS(itemStack));

            return listConstructor.newInstance(nmsItemList, nullNmsItemStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object itemToNMS(ItemStack item) throws Exception {
        return ReflectionUtils.itemStackToNMSCopyMethod.invoke(null, item);
    }
}