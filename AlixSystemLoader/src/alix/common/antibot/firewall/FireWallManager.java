package alix.common.antibot.firewall;

import alix.common.AlixCommonMain;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.file.FileManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FireWallManager {

    //Disabled for now
    private static final AlixOSFireWall osFireWall = null;//AlixOSFireWall.INSTANCE;
    public static final boolean isOsFireWallInUse = osFireWall != null;
    private static final FireWallFile file = new FireWallFile();
    private static final Map<String, FireWallEntry> map = new ConcurrentHashMap<>(65536);

    static {
        AlixScheduler.async(() -> {
            try (InputStream is = FireWallManager.class.getResourceAsStream("bad_ips.txt")) {
                FileManager.readLines(is, ip -> add0(ip, new FireWallEntry(null)));
                file.load();
                AlixCommonMain.logInfo("Fully loaded the FireWall DataBase. Total blacklisted IPs: " + map.size());
                //if(isOsFireWallInUse) osFireWall.blacklist("");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public static void addCauseException(InetAddress ip) {
        add0(ip.getHostAddress(), new FireWallEntry("ex_ca: DecoderException"));
    }

    public static boolean add(String ip, String algorithmId) {
        return add0(ip, new FireWallEntry(algorithmId)) == null;
    }

    private static void osBlacklist0(String ip) {
        try {
            osFireWall.blacklist(ip);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static FireWallEntry add0(String ip, FireWallEntry entry) {
        if (isOsFireWallInUse) osBlacklist0(ip);
        return map.put(ip, entry);
    }

    public static boolean isBlocked(InetSocketAddress address) {
        return !isOsFireWallInUse && map.containsKey(address.getAddress().getHostAddress());
    }

    public static void fastSave() {
        try {
            //AlixCommonMain.logError("Started saving firewall.txt...");
            //long t = System.nanoTime();
            file.saveKeyAndVal(map, "|", FireWallEntry::isNotBuiltIn);
            //AlixCommonMain.logError("Took " + (System.nanoTime() - t) / Math.pow(10, 6) + "ms too save the firewall file!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void onAsyncSave() {
        try {
            file.saveKeyAndVal(map, "|", FireWallEntry::isNotBuiltIn);
            AlixCommonMain.debug("Successfully saved the firewall.txt file!");
        } catch (IOException e) {
            e.printStackTrace();
            AlixCommonMain.logWarning("Could not save the firewall.txt file! Some information could be lost!");
        }
    }

    public static Map<String, FireWallEntry> getMap() {
        return map;
    }

    public static void init() {
    }

    private FireWallManager() {
    }
}