package alix.common.reflection;

import alix.common.AlixCommonMain;
import alix.common.utils.AlixCommonUtils;
import org.bukkit.Bukkit;

public final class BukkitReflection {

    public static final Object MC_SERVER_OBJ;
    public static final String serverVerRegex;//, nms;
    public static final int bukkitVersion;//In 1.20.2 will return 20
    private static final boolean protocolVersion;

    static {
        String obc0;
        int bukkitVersion0;

        /*switch (ServerEnvironment.getEnvironment()) {
            case PAPER:
            case SPIGOT:
                try {
                    //getVersion         3871-Spigot-d2eba2c-3f9263b (MC: 1.20.1)
                    //getBukkitVersion   1.20.1-R0.1-SNAPSHOT
                    String version = (String) getMethod(Bukkit.class, "getBukkitVersion").invoke(null);
                    craftBukkitVersionRegex0 = version.split("-")[0].replace('.','_');


                    CraftServer

                    serverVersion0 = ServerEnvironment.isPaper() ? "" : Bukkit.getServer().getClass().getPackage().getName().substring(23); //version.split("-")[0].replaceAll("\\.","_")
                    bukkitVersion0 = Integer.parseInt(version.split("\\.")[1].substring(0, 2));
                } catch (Exception e) {
                    System.err.println("SEND THIS TO SHADOWWWWWWW! \\/");
                    e.printStackTrace();
                    serverVersion0 = Bukkit.getServer().getClass().getPackage().getName().substring(23);
                    craftBukkitVersionRegex0 = serverVersion0;
                    bukkitVersion0 = Integer.parseInt(serverVersion0.split("_")[1]);
                }
                break;
            default:
                serverVersion0 = "";
                bukkitVersion0 = -1;
        }*/


        try {
            Class.forName("org.bukkit.craftbukkit.CraftServer");
            //getVersion         3871-Spigot-d2eba2c-3f9263b (MC: 1.20.1)
            //getBukkitVersion   1.20.1-R0.1-SNAPSHOT
            String version = (String) CommonReflection.getMethod(Bukkit.class, "getBukkitVersion").invoke(null);
            bukkitVersion0 = Integer.parseInt(maxLen(version.split("\\.")[1], 2));
            obc0 = null;
        } catch (ClassNotFoundException e) {
            //AlixCommonMain.logError("SDFGBHJKLIJUGFDXSCVBNJHUY76TRDFVBNJKI6RSDX");
            //e.printStackTrace();
            obc0 = Bukkit.getServer().getClass().getPackage().getName().substring("org.bukkit.craftbukkit.".length());
            bukkitVersion0 = Integer.parseInt(maxLen(obc0.split("_")[1], 2));
        } catch (Exception e) {
            AlixCommonUtils.logException(e);
            throw new Error();
        }

/*        try {
            Class.forName("net.minecraft.server.MinecraftServer");

        } catch (Exception e) {

        }*/


        //nms = nms0;
        serverVerRegex = obc0;
        bukkitVersion = bukkitVersion0;
        protocolVersion = bukkitVersion >= 17;
        //AlixCommonMain.logInfo cannot be used here because of initialization issues
        AlixCommonMain.logInfo("Instantiated reflections with: Version Regex: '" + serverVerRegex + "' Bukkit Version: " + bukkitVersion);
    }

    private static String maxLen(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen);
    }

/*    private static int getBukkitVersion() {
        return Integer.parseInt(serverVersion2Regex.split("_")[1]);
    }

    private static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }*/

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
            return Class.forName(String.format("net.minecraft.server.%s.%s", serverVerRegex, splitName[splitName.length - 1]));
        }
    }
}