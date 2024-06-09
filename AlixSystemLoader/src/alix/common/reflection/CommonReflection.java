package alix.common.reflection;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public final class CommonReflection {

    public static final Object MC_SERVER_OBJ;
    private static final String serverVersion2 = getServerVersion();
    private static final int bukkitVersion = getBukkitVersion();//In 1.20.2 will return 20
    //public static final int subBukkitVersion = getSubBukkitVersion();//In 1.20.2 will return 2
    private static final boolean protocolVersion = bukkitVersion >= 17;

    public static Object get(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getFieldOrNullIfAbsent(Object obj, String name) {
        try {
            return obj.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int getBukkitVersion() {
        return Integer.parseInt(serverVersion2.split("_")[1]);
    }

    private static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    static {
        Class<?> mcServerClass;
        try {
            mcServerClass = nms2("server.MinecraftServer");
            Object mcServer = mcServerClass.getMethod("getServer").invoke(null);
            MC_SERVER_OBJ = mcServer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> nms2(String name) throws ClassNotFoundException {
        if (protocolVersion) {
            return Class.forName("net.minecraft." + name);
        } else {
            String[] splitName = name.split("\\.");
            return Class.forName(String.format("net.minecraft.server.%s.%s", serverVersion2, splitName[splitName.length - 1]));
        }
    }
}