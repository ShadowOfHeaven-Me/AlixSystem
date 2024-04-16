package shadow.utils.holders.packet.constructors;


import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import org.bukkit.inventory.ItemStack;
import shadow.utils.netty.NettyUtils;

import java.util.ArrayList;
import java.util.List;

public final class OutWindowItemsPacketConstructor {

/*    private static final boolean newerConstructor;
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
        Object nullNmsItemStack0 = ReflectionUtils.fieldGet(ReflectionUtils.getFieldFromTypeDirect(nmsItemStackClazz, nmsItemStackClazz), null);


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
    }*/

    public static ByteBuf constructConst(int windowId, List<ItemStack> list) {
        return constructConst0(windowId, list.size(), createRetrooperItemList(list));
    }

    public static ByteBuf constructConst0(int windowId, int size, List<com.github.retrooper.packetevents.protocol.item.ItemStack> retrooperList) {
        return NettyUtils.constBuffer(new WrapperPlayServerWindowItems(windowId, size, retrooperList, null));
        //return newerConstructor ? construct_1_17(windowId, size, nmsList) : construct_old(windowId, nmsList);
    }

    public static ByteBuf constructDynamic0(int windowId, int size, List<com.github.retrooper.packetevents.protocol.item.ItemStack> retrooperList) {
        return NettyUtils.createBuffer(new WrapperPlayServerWindowItems(windowId, size, retrooperList, null));
        //return newerConstructor ? construct_1_17(windowId, size, nmsList) : construct_old(windowId, nmsList);
    }

/*    public static Object construct_1_17(int windowId, int size, Object nmsList) {
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
    }*/

    public static List<com.github.retrooper.packetevents.protocol.item.ItemStack> createRetrooperItemList(List<ItemStack> spigotItemList) {
        List<com.github.retrooper.packetevents.protocol.item.ItemStack> list = new ArrayList<>(spigotItemList.size());

        for (ItemStack itemStack : spigotItemList) list.add(SpigotConversionUtil.fromBukkitItemStack((itemStack)));

        return list;
        //return listConstructor.newInstance(list, nullNmsItemStack);
    }

/*    private static com.github.retrooper.packetevents.protocol.item.ItemStack bukkitItemToRetrooper(ItemStack item) throws Exception {
        ItemType type = ItemTypes.getByName(item.getType().name());
        com.github.retrooper.packetevents.protocol.item.ItemStack retrooperItemStack = new com.github.retrooper.packetevents.protocol.item.ItemStack.Builder().type(type);
        SpigotConversionUtil.fromBukkitItemStack(
        //return ReflectionUtils.itemStackToNMSCopyMethod.invoke(null, item);
    }*/

    public static void init() {
    }

    private OutWindowItemsPacketConstructor() {
    }
}