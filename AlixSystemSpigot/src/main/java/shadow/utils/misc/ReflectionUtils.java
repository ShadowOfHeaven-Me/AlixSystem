package shadow.utils.misc;

import alix.common.reflection.BukkitReflection;
import alix.common.reflection.CommonReflection;
import alix.common.utils.other.throwable.AlixError;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ReflectionUtils {

    //private static final String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    public static final String serverVerRegex = BukkitReflection.serverVerRegex;
    public static final boolean isServerVerRegexBlank = serverVerRegex == null;
    public static final int bukkitVersion = BukkitReflection.bukkitVersion;//In 1.20.2 will return 20

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

    /*public static final Class<?> outPlayerInfoPacketClass = ReflectionUtils.nms2("network.protocol.game.PacketPlayOutPlayerInfo", "network.protocol.game.ClientboundPlayerInfoPacket", "network.protocol.game.ClientboundPlayerInfoUpdatePacket");
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

*//*    public static final Class<?> enumProtocolClass = nms2("network.EnumProtocol");
    public static final Enum<?> FROM_CLIENT_DURING_HANDSHAKE = (Enum<?>) enumProtocolClass.getEnumConstants()[0];
    public static final Enum<?> FROM_CLIENT_AFTER_HANDSHAKE = (Enum<?>) enumProtocolClass.getEnumConstants()[2];

    public static final Class<?> enumProtocolDirectionClass = nms2("network.protocol.EnumProtocolDirection");

    public static final Enum<?> SERVERBOUND_PROTOCOL_DIR = (Enum<?>) enumProtocolDirectionClass.getEnumConstants()[0];

    public static final List<?> HANDSHAKE_ID_MAPPER;
    public static final List<?> AFTER_HANDSHAKE_ID_MAPPER;

    static {
        HANDSHAKE_ID_MAPPER = packetIdMapper(enumProtocolClass, FROM_CLIENT_DURING_HANDSHAKE, SERVERBOUND_PROTOCOL_DIR);
        AFTER_HANDSHAKE_ID_MAPPER = packetIdMapper(enumProtocolClass, FROM_CLIENT_AFTER_HANDSHAKE, SERVERBOUND_PROTOCOL_DIR);
    }

    private static List<?> packetIdMapper(Class<?> clazz, Object obj, Object protocolDir) {
        try {
            for (Field f : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Map<?, ?> protocolDirToSubclass = (Map<?, ?>) f.get(obj);
                    Object subclass = protocolDirToSubclass.get(protocolDir);
                    for (Field field : subclass.getClass().getDeclaredFields()) {
                        if (List.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            return (List<?>) field.get(subclass);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new AlixException(e);
        }
        throw new AlixError("Class: " + clazz.getSimpleName() + " obj: " + obj);
    }*//*

    public static final Class<?> outChatMessagePacketClass = nms2("network.protocol.game.ClientboundSystemChatPacket", "network.protocol.game.ClientboundChatPacket", "network.protocol.game.PacketPlayOutChat");
    public static final Class<?> chatMessageType = nms2("network.chat.ChatMessageType");

    public static final Class<?> outGameStatePacketClass = nms2("network.protocol.game.PacketPlayOutGameStateChange");
    //public static final Constructor<?> outGameStatePacketConstructor = getConstructor(outGameStatePacketClass, int.class, float.class);
    //public static final Object ADVENTURE_MODE_PACKET = newInstance(outGameStatePacketConstructor, 3, 2);//3 for gamemode, 2 for adventure

    public static final Class<?> outHeldItemSlotPacketClass = nms2("network.protocol.game.PacketPlayOutHeldItemSlot");
*/
    //public static final Class<?> inWindowClickPacketClass = nms2("network.protocol.game.PacketPlayInWindowClick");
    //public static final Field inWindowClickSlotField = getFieldFromTypeSafe(inWindowClickPacketClass, int.class, 1);

    //public static final Class<?> outWindowOpenPacketClass = nms2("network.protocol.game.PacketPlayOutOpenWindow");
    //public static final Method outWindowOpenIdMethod = getMethodByReturnType(outWindowOpenPacketClass, int.class);
    //public static final Field outWindowOpenIdField = getFieldFromTypeSafe(outWindowOpenPacketClass, int.class);

    //public static final Class<?> nonNullListClass = nms2("core.NonNullList");
    //public static final Class<?> craftItemStackClass = obc("inventory.CraftItemStack");
    ///public static final Method itemStackToNMSCopyMethod = getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    //public static final Class<?> nmsItemStackClass = itemStackToNMSCopyMethod.getReturnType();

    // public static final Class<?> outWindowItemsPacketClass = nms2("network.protocol.game.PacketPlayOutWindowItems");
    //public static final Method outWindowItemsIdMethod = getMethodByReturnType(outWindowItemsPacketClass, int.class);


    //public static final Class<?> inItemNamePacketClass = nms2("network.protocol.game.PacketPlayInItemName");
    //public static final Method inItemNamePacketTextMethod = getMethodByReturnType(inItemNamePacketClass, String.class);


/*
    public static final Class<?> inKeepAlivePacketClass = nms2("network.protocol.game.PacketPlayInKeepAlive", "network.protocol.common.ServerboundKeepAlivePacket");
    //public static final Class<?> outKeepAlivePacketClass = nms2("network.protocol.game.PacketPlayOutKeepAlive");
    //public static final Method getKeepAliveMethod = getMethodByReturnType(inKeepAlivePacketClass, long.class);
    //public static final Constructor<?> outKeepAliveConstructor = getConstructor(outKeepAlivePacketClass, long.class);


    public static final Class<?> outExperiencePacketClass = nms2("network.protocol.game.PacketPlayOutExperience");
    public static final Constructor<?> outExperienceConstructor = getConstructor(outExperiencePacketClass, float.class, int.class, int.class);


    public static final Class<?> outEntityEffectPacketClass = nms2("network.protocol.game.PacketPlayOutEntityEffect");
    public static final Constructor<?> outEntityEffectConstructor = getConstructor(outEntityEffectPacketClass, int.class, mobEffectClass);

    public static final Constructor<?> mobEffectConstructor = getConstructor(mobEffectClass, mobEffectListClass, int.class, int.class, boolean.class, boolean.class, boolean.class);
    public static final MobEffectSupplier mobEffectFromId = MobEffectLookup.getSupplier(() -> getMethodByReturnType(mobEffectListClass, mobEffectListClass, int.class));
    private static final Object BLINDNESS_MOB_EFFECT_LIST = mobEffectFromId.toNMSEffectTypeFromId(15); //invoke(mobEffectListFromIdMethod, null, 15);


    public static final Class<?> outRemoveEntityEffectPacketClass = nms2("network.protocol.game.PacketPlayOutRemoveEntityEffect");
    public static final Constructor<?> outRemoveEntityEffectPacketConstructor = getConstructor(outRemoveEntityEffectPacketClass, int.class, mobEffectListClass);


    public static final Class<?> outMapPacketClass = nms2("network.protocol.game.PacketPlayOutMap");

    public static final Class<?> craftChatMessageClass = obc("util.CraftChatMessage");
    public static final Method IChatComponentArrayFromStringMethod = getMethod(craftChatMessageClass, "fromString", String.class);
    public static final Class<?> IChatBaseComponentClass = IChatComponentArrayFromStringMethod.getReturnType().getComponentType();

    public static final Class<?> handshakePacketClass = nms2("network.protocol.handshake.PacketHandshakingInSetProtocol");
    public static final Class<?> loginInStartPacketClass = nms2("network.protocol.login.PacketLoginInStart");

    public static final Class<?> disconnectKickPlayPhasePacketClass = nms2("network.protocol.game.PacketPlayOutKickDisconnect", "network.protocol.common.ClientboundDisconnectPacket");
    public static final Class<?> disconnectLoginPhasePacketClass = nms2("network.protocol.login.PacketLoginOutDisconnect", "network.protocol.login.ClientboundLoginDisconnectPacket");
*/


    /*public static final Class<?> enumHandClass = nms2("world.InteractionHand", "EnumHand");
    public static final Enum<?> enumMainHand = (Enum<?>) enumHandClass.getEnumConstants()[0];
    public static final Class<?> openBookPacketClass = nms2("network.protocol.game.ClientboundOpenBookPacket","network.protocol.game.PacketPlayOutOpenBook");//the 2nd one isn't an actual packet class name, it's just used for the nms2 method to work properly
    public static final Constructor<?> bookPacketConstructor = getConstructor(openBookPacketClass, enumHandClass);*/

    /*
    public static final Class<?> outDestroyEntityPacketClass = nms2("network.protocol.game.PacketPlayOutEntityDestroy");
    public static final Constructor<?> outDestroyEntityConstructor = getConstructor(outDestroyEntityPacketClass, int[].class);
*/
    //public static final Class<?> packetClazz = ReflectionUtils.nms2("network.protocol.Packet");

    //public static final Class<?> enumProtocolDirectionClazz = ReflectionUtils.nms2("network.protocol.EnumProtocolDirection");
    //https://mappings.cephx.dev/1.20.6/net/minecraft/network/protocol/PacketFlow.html
    //public static final Enum clientboundProtocolDirection = (Enum) ReflectionUtils.enumProtocolDirectionClazz.getEnumConstants()[1];

    //public static final Class<?> packetDataSerializerClass = nms2("network.PacketDataSerializer");
    //public static final Constructor<ByteBuf> packetDataSerializerConstructor = (Constructor<ByteBuf>) getConstructor(packetDataSerializerClass, ByteBuf.class);

    //public static final Class<?> mobEffectClass = nmsClazz("net.minecraft.server.%s.MobEffect", "net.minecraft.world.effect.MobEffect");
    //public static final Class<?> mobEffectListClass = nmsClazz("net.minecraft.server.%s.MobEffectList", "net.minecraft.world.effect.MobEffectList");

    //always get what the CraftBukkit implementation uses
    public static final Class<?> entityLivingClass = ReflectionUtils.getMethod(obc("entity.CraftLivingEntity"), "getHandle").getReturnType();
    public static final Class<?> entityPlayerClass = nms2("server.level.EntityPlayer", "server.level.ServerPlayer");
    public static final Class<?> craftPlayerClass = obc("entity.CraftPlayer");
    //public static final Class<?> playerConnectionClass = nms2("server.network.PlayerConnection");
    //public static final Class<?> networkManagerClass = nms2("network.NetworkManager");


    //public static final Method getProfile = getMethodByReturnType(entityPlayerClass, GameProfile.class);
    public static final Method getHandle = getMethod(craftPlayerClass, "getHandle");//CraftPlayer -> EntityPlayer
    public static final Method getBukkitEntity_CraftPlayer = getMethod(entityPlayerClass, "getBukkitEntity");//EntityPlayer -> CraftPlayer
    public static final CommandMap commandMap = getCommandMap();
    public static final Map<String, Command> serverKnownCommands = getKnownCommands();
    public static final YamlConfiguration serverConfiguration = getServerConfiguration();

/*    public static void initCrashSave() {

    }*/

/*    public static Object[] constructTextComponents(String text) {
        try {
            return (Object[]) IChatComponentArrayFromStringMethod.invoke(null, text);
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }*/

    public static String methodsToString(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        for (Method method : clazz.getDeclaredMethods()) {
            sb.append(method).append('\n');
        }
        return sb.substring(0, Math.max(0, sb.length() - 1));
    }

    public static boolean invokeIfPresent(Method method, Object obj, Object... args) {
        if (method == null) return false;

        try {
            method.invoke(obj, args);
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethodOrNull(Class<?> clazz, String name, Class<?>... params) {
        try {
            return clazz.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Object getHandle(Player player) {
        try {
            return getHandle.invoke(player);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void replaceBansToConcurrent() {
        replaceToConcurrent0(Bukkit.getBanList(BanList.Type.NAME));
        replaceToConcurrent0(Bukkit.getBanList(BanList.Type.IP));
    }

    private static void replaceToConcurrent0(BanList bukkitList) {
        try {
            Field f = bukkitList.getClass().getDeclaredField("list");
            f.setAccessible(true);
            Object nmsList = f.get(bukkitList);
            Class<?> nmsListClazz = nms2("server.players.JsonList", "server.players.StoredUserList");
            for (Field f2 : nmsListClazz.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f2.getType()) && !ConcurrentMap.class.isAssignableFrom(f2.getType())) {
                    f2.setAccessible(true);
                    Map<?, ?> hashMap = (Map<?, ?>) f2.get(nmsList);
                    f2.set(nmsList, new ConcurrentHashMap<>(hashMap));
                    return;
                }
            }
            throw new RuntimeException("Not found: " + Arrays.toString(nmsListClazz.getDeclaredFields()));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

/*    public static Object createMaxedEffectPacket(int entityId, Object mobEffectList) {
        try {
            //Object mobEffectList = mobEffectFromId.toNMSEffectTypeFromId(effectId); // mobEffectFromId.invoke(null, effectId); //mobEffectListClass.getDeclaredMethod("fromId", int.class).invoke(null, effectId);
            Object mobEffect = mobEffectConstructor.newInstance(mobEffectList, 999999999, 255, false, false, false);

            return outEntityEffectConstructor.newInstance(entityId, mobEffect);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

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

    //private static final Unsafe unsafe = AlixUnsafe.getUnsafe();
    //private static final long MAP_OFFSET = AlixUnsafe.fieldOffset(Player.class,

    private static Map<String, Command> getKnownCommands() {
        try {
            Method m = commandMap.getClass().getMethod("getKnownCommands");
            return (Map<String, Command>) m.invoke(commandMap);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static CommandMap getCommandMap() {
        Server s = Bukkit.getServer();
        Field f = getFieldFromTypeAssignable(s.getClass(), CommandMap.class);
        try {
            return (CommandMap) f.get(s);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static YamlConfiguration getServerConfiguration() {
        Server s = Bukkit.getServer();
        try {
            Field f = s.getClass().getDeclaredField("configuration");
            f.setAccessible(true);
            return (YamlConfiguration) f.get(s);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    public static void setConnectionThrottle(Long value) {
        serverConfiguration.set("settings.connection-throttle", value);
        Server s = Bukkit.getServer();
        try {
            Method method = s.getClass().getDeclaredMethod("saveConfig");
            method.setAccessible(true);
            method.invoke(s);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    public static void reloadCommands() {
        Server s = Bukkit.getServer();
        try {
            Method method = s.getClass().getDeclaredMethod("syncCommands");
            method.setAccessible(true);
            method.invoke(s);
        } catch (Exception e) {
            //throw new InternalError(e);
        }
    }

//    private static void syncCommandsOld() {
//
//        CommandMap bukkitCommandMap = getCommandMap();
//        CommandDispatcher dispatcher = getNmsCommandDispatcher();
//
//        try {
//            Field knownCommandsField = CommandDispatcher.class.getDeclaredField("a");
//            knownCommandsField.setAccessible(true);
//            Map<String, Command> nmsKnownCommands = (Map<String, Command>) knownCommandsField.get(dispatcher);
//
//            // Iterate through Bukkit commands and register them with the NMS dispatcher
//            for (Command command : bukkitCommandMap.getCommands()) {
//                nmsKnownCommands.put(command.getName(), command);
//                for (String alias : command.getAliases()) {
//                    nmsKnownCommands.put(alias, command);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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

    public static Field getFieldAccessible(Class<?> clazz, String... names) {
        Field field = null;
        for (String name : names) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
            if (field != null) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public static Field getDeclaredField(Class<?> clazz, String name) {
        return CommonReflection.getDeclaredField(clazz, name);
    }

    public static Object getFieldResult(Class<?> clazz, String name, Object obj) {
        return CommonReflection.getFieldResult(clazz, name, obj);
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

    public static Class<?> nmsClazzOrNull(String oldPath, String newPath) {
        try {
            return nmsClazz(oldPath, newPath);
        } catch (Exception e) {
            return null;
        }
    }

    public static Class<?> nmsClazz(String oldPath, String newPath) {
        if (protocolVersion) {
            return forName(newPath);
        } else {
            return forName(String.format(oldPath, serverVerRegex));
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
            if (isServerVerRegexBlank)
                return Class.forName(String.format("net.minecraft.server.%s", splitName[splitName.length - 1]));
            return Class.forName(String.format("net.minecraft.server.%s.%s", serverVerRegex, splitName[splitName.length - 1]));
        }
    }

    public static Class<?> nms2(String name) {
        try {
            return nms2WithThrowable(name);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

/*    private static int getBukkitVersion() {
        return Integer.parseInt(serverVersion2.split("_")[1]);
    }

*//*    private static int getSubBukkitVersion() {
        String[] m = serverVersion2.split("_");
        return m.length == 3 ? Integer.parseInt(m[2]) : 0;
    }*//*

    //getVersion         3871-Spigot-d2eba2c-3f9263b (MC: 1.20.1)
    //getBukkitVersion   1.20.1-R0.1-SNAPSHOT
    private static String getServerVersion() {
        try {
            String version = (String) getMethod(Bukkit.class, "getBukkitVersion").invoke(null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }*/

    public static Method getStringMethodFromPacketClass(Class<?> clazz) {
        for (Method m : clazz.getMethods())
            if (!m.getName().equals("toString") && m.getReturnType() == String.class)
                return m;
        throw new Error("Class: " + clazz);
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
        if (isServerVerRegexBlank) return forName(String.format("org.bukkit.craftbukkit.%s", name));
        return forName(String.format("org.bukkit.craftbukkit.%s.%s", serverVerRegex, name));
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

    public static Field getFieldByTypeAndParamsOrNull(Class<?> instClass, Class<?> fieldTypeSuperclass, Class<?>... params) {
        for (Field field : instClass.getDeclaredFields())
            if (fieldTypeSuperclass.isAssignableFrom(field.getType())) {
                Type t = field.getGenericType();
                if (t instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) t;
                    if (Arrays.equals(type.getActualTypeArguments(), params)) {
                        field.setAccessible(true);
                        return field;
                    }
                }
            }
        return null;
        //throw new ExceptionInInitializerError("No valid field with " + fieldTypeSuperclass + " in " + instClass);
    }

    public static Field getFieldByType(Class<?> instClass, Class<?> typeClass) {
        return CommonReflection.getFieldAccessibleByType(instClass, typeClass);
    }

    private static Field getLastFieldByType(Class<?> instClass, Class<?> typeClass) {
        Field field = null;
        for (Field f : instClass.getDeclaredFields()) if (f.getType() == typeClass) field = f;
        if (field == null)
            throw new ExceptionInInitializerError("No valid field with " + typeClass + " in " + instClass);
        field.setAccessible(true);
        return field;
    }

    public static Method getMethodByName(Class<?> clazz, String name) {
        try {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    m.setAccessible(true);
                    return m;
                }
            }
        } catch (Exception e) {
            throw new AlixError(e, "No method: " + clazz.getSimpleName() + "." + name);
        }
        throw new AlixError("No method: " + clazz.getSimpleName() + "." + name);
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... params) {
        return CommonReflection.getMethod(clazz, name, params);
    }

    public static Method getDeclaredMethod(Class<?> clazz, String... possibleNames) {
        for (Method method : clazz.getDeclaredMethods()) {
            for (String name : possibleNames) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Method getMethodByReturnType(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
        for (Method method : clazz.getMethods())
            if (method.getReturnType() == returnType && equalsArrayCheck(parameterTypes, method.getParameterTypes()))
                return method;
        throw new ExceptionInInitializerError(new NoSuchMethodException("No valid method returning: " + returnType + " in " + clazz));
    }

    public static Method getMethodByReturnTypeByFirstParams(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
        for (Method method : clazz.getMethods()) {
            if (method.getReturnType() == returnType && equalsArrayCheck(parameterTypes, Arrays.copyOf(method.getParameterTypes(), parameterTypes.length)))
                return method;
        }
        throw new ExceptionInInitializerError(new NoSuchMethodException("No valid method returning: " + returnType + " in " + clazz));
    }

    public static Method getMethodByReturnTypeAssignable(Class<?> instance, Class<?> returnType, Class<?>... parameterTypes) {
        for (Method method : instance.getMethods()) {
            if (returnType.isAssignableFrom(method.getReturnType()) && equalsArrayCheck(parameterTypes, method.getParameterTypes()))
                return method;
        }
        //AlixUtils.debug(instance.getDeclaredMethods());
        throw new ExceptionInInitializerError(new NoSuchMethodException("No valid method returning: " + returnType + " in " + instance));
    }

    private static boolean equalsArrayCheck(Object[] a1, Object[] a2) {
        if (a1.length != a2.length) return false;
        for (int i = 0; i < a1.length; i++) if (!Objects.equals(a1[i], a2[i])) return false;
        return true;
    }

    public static Constructor<?> getConstructor(Class<?> instanceClass, Class<?>... parameterTypes) {
        try {
            return instanceClass.getConstructor(parameterTypes);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static <T> T newInstance(Constructor<T> c, Object... args) {
        try {
            return c.newInstance(args);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ReflectionUtils() {
    }
}