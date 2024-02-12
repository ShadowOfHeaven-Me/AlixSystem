package alix.common.antibot.firewall;

import alix.common.AlixCommonMain;
import alix.common.antibot.firewall.os.AlixOSFireWall;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.file.FileManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FireWallManager {

    public static final AlixOSFireWall osFireWall = AlixOSFireWall.INSTANCE;
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

    public static boolean addCauseException(String ip, Throwable e) {
        String msg = e.toString().replaceAll("\n", "");
        return add0(ip, new FireWallEntry("ex_ca:" + msg)) == null;
    }

    public static boolean add(String ip, String algorithmId) {
        return add0(ip, new FireWallEntry(algorithmId)) == null;
    }

    static FireWallEntry add0(String ip, FireWallEntry entry) {
        if (isOsFireWallInUse) {
            try {
                osFireWall.blacklist(ip);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return map.put(ip, entry);
    }

    public static boolean isBlocked(InetSocketAddress address) {
        return !isOsFireWallInUse && map.containsKey(address.getAddress().getHostAddress());
    }

    public static void fastSave() {
        try {
            file.saveKeyAndVal(map, "|", FireWallEntry::isNotBuiltIn);
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