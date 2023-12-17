package shadow.utils.holders;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import shadow.utils.holders.packet.constructors.OutMapPacketConstructor;
import shadow.utils.holders.packet.constructors.OutPlayerInfoPacketConstructor;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ReflectionUtils {

    //private static final String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    public static final String serverVersion2 = getServerVersion();
    public static final int bukkitVersion = getBukkitVersion();//In 1.20.2 will return 20
    //public static final int subBukkitVersion = getSubBukkitVersion();//In 1.20.2 will return 2
    public static final boolean protocolVersion = bukkitVersion >= 17;
    //private static final int ver = Integer.parseInt(serverVersion.split("_")[1]);*/

    /*     public static final Class<?> craftPlayerClass = obc("entity.CraftPlayer");
         //public static final Class<?> craftWorldClass = obc("CraftWorld");
         public static final Class<?> entityPlayerClass = nms("server.level.EntityPlayer");
         public static final Class<?> playerConnectionClass = nms("server.network.PlayerConnection");
         public static final Class<?> networkManagerClass = nms("network.NetworkManager");*/
    //public static final Class<?> enumGamemodeClass = nms("world.level.EnumGamemode");
    //public static final Class<?> worldClass = nms("world.level.World");
    //public static final Class<?> dimensionManagerClass = nms("world.level.dimension.DimensionManager");
    //public static final Class<?> outPlayerInfoPacketClass = nms2("network.protocol.game.PacketPlayOutPlayerInfo", "network.protocol.game.ClientboundPlayerInfoPacket");
    //public static final Class<?> outPlayerInfoPacketClass$EnumPlayerInfoAction = outPlayerInfoPacketClass.getClasses()[0];
    //public static final Field dimensionManagerField = getFieldByType(worldClass, dimensionManagerClass);
    //public static final Field resourceWorldField = getLastFieldByType(worldClass, ResourceKey.class);
/*     public static final Field channelField = getFieldByType(networkManagerClass, Channel.class);
     public static final Field playerConnectionField = getFieldByType(entityPlayerClass, playerConnectionClass);
     public static final Field networkManagerField = getFieldByType(playerConnectionClass, networkManagerClass);*/
    //public static final Object[] enumGameModes = enumGamemodeClass.getEnumConstants();
    //public static final Object ADD_PLAYER = outPlayerInfoPacketClass$EnumPlayerInfoAction.getEnumConstants()[0];
    //public static final Object REMOVE_PLAYER = outPlayerInfoPacketClass$EnumPlayerInfoAction.getEnumConstants()[4];
    //public static final Object DISPLAY_NAME_UPDATE = packetPlayOutPlayerInfoClass$EnumPlayerInfoAction.getEnumConstants()[3];
    //public static final Constructor<?> packetPlayOutPlayerInfoConstructor = getConstructor(outPlayerInfoPacketClass, outPlayerInfoPacketClass$EnumPlayerInfoAction, Collection.class);

    public static final Class<?> outPlayerInfoPacketClass = ReflectionUtils.nms2("network.protocol.game.PacketPlayOutPlayerInfo", "network.protocol.game.ClientboundPlayerInfoPacket", "network.protocol.game.ClientboundPlayerInfoUpdatePacket");
    //public static final Method getPlayerInfoDataListMethod = getMethodByReturnType(outPlayerInfoPacketClass, List.class);
    public static final Class<?> playerInfoDataClass;
    public static final Method getProfileFromPlayerInfoDataMethod;

    static {
        Class<?> playerInfoDataClazz = null;
        Method method = null;
        for (Class<?> clazz : outPlayerInfoPacketClass.getClasses()) {
            for (Method m : clazz.getMethods()) {
                if (m.getReturnType() == GameProfile.class) {
                    playerInfoDataClazz = clazz;
                    method = m;
                    break;
                }
            }
        }
        assert playerInfoDataClazz != null;
        assert method != null;
        playerInfoDataClass = playerInfoDataClazz;
        getProfileFromPlayerInfoDataMethod = method;
    }

    public static final Class<?> outHeldItemSlotPacketClass = nms2("network.protocol.game.PacketPlayOutHeldItemSlot");

    //public static final Class<?> inWindowClickPacketClass = nms2("network.protocol.game.PacketPlayInWindowClick");
    //public static final Field inWindowClickSlotField = getFieldFromTypeSafe(inWindowClickPacketClass, int.class, 1);

    public static final Class<?> outWindowOpenPacketClass = nms2("network.protocol.game.PacketPlayOutOpenWindow");
    //public static final Method outWindowOpenIdMethod = getMethodByReturnType(outWindowOpenPacketClass, int.class);
    //public static final Field outWindowOpenIdField = getFieldFromTypeSafe(outWindowOpenPacketClass, int.class);

    public static final Class<?> nonNullListClass = nms2("core.NonNullList");
    public static final Class<?> craftItemStackClass = obc("inventory.CraftItemStack");
    public static final Method itemStackToNMSCopyMethod = getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    public static final Class<?> nmsItemStackClass = itemStackToNMSCopyMethod.getReturnType();

    public static final Class<?> outWindowItemsPacketClass = nms2("network.protocol.game.PacketPlayOutWindowItems");
    //public static final Method outWindowItemsIdMethod = getMethodByReturnType(outWindowItemsPacketClass, int.class);


    public static final Class<?> inItemNamePacketClass = nms2("network.protocol.game.PacketPlayInItemName");
    public static final Method inItemNamePacketTextMethod = getMethodByReturnType(inItemNamePacketClass, String.class);


    public static final Class<?> inKeepAlivePacketClass = nms2("network.protocol.game.PacketPlayInKeepAlive", "network.protocol.common.ServerboundKeepAlivePacket");
    //public static final Class<?> outKeepAlivePacketClass = nms2("network.protocol.game.PacketPlayOutKeepAlive");
    //public static final Method getKeepAliveMethod = getMethodByReturnType(inKeepAlivePacketClass, long.class);
    //public static final Constructor<?> outKeepAliveConstructor = getConstructor(outKeepAlivePacketClass, long.class);


    public static final Class<?> outExperiencePacketClass = nms2("network.protocol.game.PacketPlayOutExperience");
    public static final Constructor<?> outExperienceConstructor = getConstructor(outExperiencePacketClass, float.class, int.class, int.class);


    public static final Class<?> outEntityEffectPacketClass = nms2("network.protocol.game.PacketPlayOutEntityEffect");
    public static final Class<?> mobEffectClass = nmsClazz("net.minecraft.server.%s.MobEffect", "net.minecraft.world.effect.MobEffect");
    public static final Constructor<?> outEntityEffectConstructor = getConstructor(outEntityEffectPacketClass, int.class, mobEffectClass);


    public static final Class<?> mobEffectListClass = nmsClazz("net.minecraft.server.%s.MobEffectList", "net.minecraft.world.effect.MobEffectList");
    public static final Constructor<?> mobEffectConstructor = getConstructor(mobEffectClass, mobEffectListClass, int.class, int.class, boolean.class, boolean.class, boolean.class);
    public static final MobEffectSupplier mobEffectFromId = MobEffectLookup.getSupplier(() -> getMethodByReturnType(mobEffectListClass, mobEffectListClass, int.class));
    private static final Object BLINDNESS_MOB_EFFECT_LIST = mobEffectFromId.toNMSEffectTypeFromId(15); //invoke(mobEffectListFromIdMethod, null, 15);


    public static final Class<?> outRemoveEntityEffectPacketClass = nms2("network.protocol.game.PacketPlayOutRemoveEntityEffect");
    public static final Constructor<?> outRemoveEntityEffectPacketConstructor = getConstructor(outRemoveEntityEffectPacketClass, int.class, mobEffectListClass);


    public static final Class<?> outMapPacketClass = nms2("network.protocol.game.PacketPlayOutMap");

    /*
    public static final Class<?> outDestroyEntityPacketClass = nms2("network.protocol.game.PacketPlayOutEntityDestroy");
    public static final Constructor<?> outDestroyEntityConstructor = getConstructor(outDestroyEntityPacketClass, int[].class);
*/


    public static final Class<?> entityPlayerClass = nms2("server.level.EntityPlayer");
    public static final Class<?> craftPlayerClass = obc("entity.CraftPlayer");
    public static final Class<?> playerConnectionClass = nms2("server.network.PlayerConnection");
    public static final Class<?> networkManagerClass = nms2("network.NetworkManager");
    public static final Method getProfile = getMethodByReturnType(entityPlayerClass, GameProfile.class);
    public static final Method getHandle = getMethod(craftPlayerClass, "getHandle");
    public static final CommandMap commandMap = getCommandMap();
    public static final Map<String, Command> serverKnownCommands = getKnownCommands();
    public static final YamlConfiguration serverConfiguration = getServerConfiguration();

/*    public static void sendMap(Channel channel, byte[] toDrawPixels, int mapViewId) {

    }*/

    public static Object createMaxedEffectPacket(int entityId, Object mobEffectList) {
        try {
            //Object mobEffectList = mobEffectFromId.toNMSEffectTypeFromId(effectId); // mobEffectFromId.invoke(null, effectId); //mobEffectListClass.getDeclaredMethod("fromId", int.class).invoke(null, effectId);
            Object mobEffect = mobEffectConstructor.newInstance(mobEffectList, 999999999, 255, false, false, false);

            return outEntityEffectConstructor.newInstance(entityId, mobEffect);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

/*    public static Object createPlayerAddPacket(Collection<Object> nmsPlayerList) {
        try {
            return packetPlayOutPlayerInfoConstructor.newInstance(ADD_PLAYER, nmsPlayerList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

/*    public static Object getNmsPlayer(Player player) {
        try {
            return getHandle.invoke(player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    public static void resetLoginEffectPackets(UnverifiedUser user) {
        int entityId = user.getPlayer().getEntityId();
        Channel channel = user.getPacketBlocker().getChannel();

        try {
            List<Object> list = new ArrayList<>(Bukkit.getOnlinePlayers().size());
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(getHandle.invoke(p));
            }

            Object addPlayersPacket = OutPlayerInfoPacketConstructor.constructADD(list);
            channel.writeAndFlush(addPlayersPacket);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        resetBlindnessEffectPackets(entityId, channel);
    }

    public static void resetBlindnessEffectPackets(int entityId, Channel channel) {
        try {
            Object removeBlindnessPacket = outRemoveEntityEffectPacketConstructor.newInstance(entityId, BLINDNESS_MOB_EFFECT_LIST);

            channel.writeAndFlush(removeBlindnessPacket);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendLoginEffectPackets(int entityId, Channel channel) {
/*        int slownessEffectId = 2;

        Object slownessPacket = createMaxedEffectPacket(entityId, slownessEffectId);

        channel.writeAndFlush(slownessPacket);


        int jumpBoostEffectId = 8;

        Object jumpBoostPacket = createMaxedEffectPacket(entityId, jumpBoostEffectId);

        channel.writeAndFlush(jumpBoostPacket);*/

        Object blindnessPacket = createMaxedEffectPacket(entityId, BLINDNESS_MOB_EFFECT_LIST);

        channel.writeAndFlush(blindnessPacket);
    }

    private static Map<String, Command> getKnownCommands() {
        try {
            Method m = commandMap.getClass().getMethod("getKnownCommands");
            return (Map<String, Command>) m.invoke(commandMap);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static CommandMap getCommandMap() {
        Object s = Bukkit.getServer();
        Field f = getFieldFromTypeAssignable(s.getClass(), CommandMap.class);
        try {
            return (CommandMap) f.get(s);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static YamlConfiguration getServerConfiguration() {
        Object s = Bukkit.getServer();
        try {
            Field f = s.getClass().getDeclaredField("configuration");
            f.setAccessible(true);
            return (YamlConfiguration) f.get(s);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    public static void setConnectionThrottle(long value) {
        serverConfiguration.set("settings.connection-throttle", value);
        Object s = Bukkit.getServer();
        try {
            Method method = s.getClass().getDeclaredMethod("saveConfig");
            method.setAccessible(true);
            method.invoke(s);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    public static void reloadCommands() {
        Object s = Bukkit.getServer();
        try {
            Method method = s.getClass().getDeclaredMethod("syncCommands");
            method.setAccessible(true);
            method.invoke(s);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

/*    public static boolean isAuthenticatedWithMojang(String name, String serverId) {
        //String serverId = player.getUniqueId().toString().replace("-", "");
        try {
            String url = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + name + "&serverId=" + serverId;
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.connect();
            try (InputStream inputStream = connection.getInputStream()) {
                JsonObject response = new JsonParser().parse(new InputStreamReader(inputStream)).getAsJsonObject();
                return response.has("id");
            }
        } catch (IOException e) {
            Main.logError("Failed to authenticate player " + name + " with session ID " + serverId);
            e.printStackTrace();
            return false;
        }
    }*/

/*    public static boolean isPlayerAuthenticated(Player p) {
        try {
            //GameProfile profile = (GameProfile) p.getClass().getMethod("getProfile").invoke(p);
            //Bukkit.broadcastMessage(profile.getName());
            Object getHandle = getMethod(p, "getHandle");
            Object playerConnection = ReflectionUtils.getPlayerConnection.get(getHandle);
            Object networkManager = ReflectionUtils.getNetworkManager.get(playerConnection);
            UUID uuid = (UUID) getField(networkManager, "spoofedUUID").get(networkManager);
            CraftPlayer p2 = null;
            p2.getHandle().playerConnection.networkManager.spoofedUUID
            return profile.isComplete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    public static Object invoke(Method method, Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Method getMethod(Class<?> clazz, String method) {
        try {
            return clazz.getMethod(method);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getField(Object o, String name) {
        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getName().equals(name)) return field;
        }
        throw new ExceptionInInitializerError();
    }


    public static Object fieldGet(Field field, Object o) {
        try {
            return field.get(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getFieldFromTypeDirect(Class<?> from, Class<?> fieldType, int fieldIndex) {
        Field field = from.getDeclaredFields()[fieldIndex];
        field.setAccessible(true);
        if (field.getType() == fieldType) return field;

        throw new ExceptionInInitializerError("getField with class " + from.getSimpleName() + " and field type " + fieldType.getSimpleName()
                + " had no declared fields!");
    }

    public static Field getFieldFromTypeDirect(Class<?> from, Class<?> fieldType) {
        for (Field field : from.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType() == fieldType) return field;
        }
        throw new ExceptionInInitializerError("getFieldFromTypeSafe with class " + from.getSimpleName() + " and field type " + fieldType.getSimpleName()
                + " had no declared fields!");
    }

    public static Field getFieldFromTypeAssignable(Class<?> from, Class<?> fieldType) {
        for (Field field : from.getDeclaredFields()) {
            field.setAccessible(true);
            if (fieldType.isAssignableFrom(field.getType())) return field;
        }
        //AlixUtils.debug(from.getDeclaredFields());
        throw new ExceptionInInitializerError("getFieldFromType with class " + from.getSimpleName() + " and field type " + fieldType.getSimpleName()
                + " had no declared fields!");
    }

    public static Class<?> nmsClazz(String oldPath, String newPath) {
        if (protocolVersion) {
            return forName(newPath);
        } else {
            return forName(String.format(oldPath, serverVersion2));
        }
    }

    public static Class<?> nms2(String... possibleNames) {
        for (String name : possibleNames) {
            try {
                return nms2WithThrowable(name);
            } catch (ClassNotFoundException ignored) {

            }
        }
        throw new ExceptionInInitializerError("The possible class names: " + Arrays.toString(possibleNames) + " were all not found!");
    }

    private static Class<?> nms2WithThrowable(String name) throws ClassNotFoundException {
        if (protocolVersion) {
            return Class.forName("net.minecraft." + name);
        } else {
            String[] splitName = name.split("\\.");
            return Class.forName(String.format("net.minecraft.server.%s.%s", serverVersion2, splitName[splitName.length - 1]));
        }
    }

    public static Class<?> nms2(String name) {
        if (protocolVersion) {
            return forName(String.format("net.minecraft.%s", name));
        } else {
            String[] splitName = name.split("\\.");
            return forName(String.format("net.minecraft.server.%s.%s", serverVersion2, splitName[splitName.length - 1]));
        }
    }

    private static int getBukkitVersion() {
        return Integer.parseInt(serverVersion2.split("_")[1]);
    }

    private static int getSubBukkitVersion() {
        String[] m = serverVersion2.split("_");
        return m.length == 3 ? Integer.parseInt(m[2]) : 0;
    }

    private static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

/*    private static Class<?> nms(String name) {
        Class<?> clazz;
        if (ver < 17) {
            String[] splitName = name.split("\\.");
            clazz = forName(String.format("net.minecraft.server.%s.%s", serverVersion, splitName[splitName.length - 1]));
        } else clazz = forName(String.format("net.minecraft.%s", name));
        return clazz;
    }*/

    public static Class<?> obc(String name) {
        return forName(String.format("org.bukkit.craftbukkit.%s.%s", serverVersion2, name));
    }

    private static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Class<?> forName(String... names) {
        for (String s : names) {
            try {
                return Class.forName(s);
            } catch (ClassNotFoundException ignored) {

            }
        }
        throw new ExceptionInInitializerError("Not found: " + Arrays.toString(names));
    }

    private static Field getFieldByType(Class<?> instClass, Class<?> typeClass) {
        for (Field field : instClass.getDeclaredFields())
            if (field.getType() == typeClass) {
                field.setAccessible(true);
                return field;
            }
        throw new ExceptionInInitializerError("No valid field with " + typeClass + " in " + instClass);
    }

    private static Field getLastFieldByType(Class<?> instClass, Class<?> typeClass) {
        Field field = null;
        for (Field f : instClass.getDeclaredFields()) if (f.getType() == typeClass) field = f;
        if (field == null)
            throw new ExceptionInInitializerError("No valid field with " + typeClass + " in " + instClass);
        field.setAccessible(true);
        return field;
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... classes) {
        try {
            return clazz.getMethod(name, classes);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Method getMethodByReturnType(Class<?> instance, Class<?> returnType, Class<?>... parameterTypes) {
        for (Method method : instance.getMethods()) {
            if (method.getReturnType() == returnType && AlixUtils.equalsArrayCheck(parameterTypes, method.getParameterTypes()))
                return method;
        }
        throw new ExceptionInInitializerError(new NoSuchMethodException("No valid method returning: " + returnType + " in " + instance));
    }

    public static Method getMethodByReturnTypeAssignable(Class<?> instance, Class<?> returnType, Class<?>... parameterTypes) {
        for (Method method : instance.getMethods()) {
            if (returnType.isAssignableFrom(method.getReturnType()) && AlixUtils.equalsArrayCheck(parameterTypes, method.getParameterTypes()))
                return method;
        }
        //AlixUtils.debug(instance.getDeclaredMethods());
        throw new ExceptionInInitializerError(new NoSuchMethodException("No valid method returning: " + returnType + " in " + instance));
    }

    public static Constructor<?> getConstructor(Class<?> instanceClass, Class<?>... parameterTypes) {
        try {
            return instanceClass.getConstructor(parameterTypes);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void init() {
        OutMapPacketConstructor.init();
        OutWindowItemsPacketConstructor.init();
        OutPlayerInfoPacketConstructor.init();
    }

    private ReflectionUtils() {
    }
}