package alix.common.antibot.firewall;

import alix.common.AlixCommonMain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FireWallManager {

    private static final FireWallFile file = new FireWallFile();
    private static final Map<String, FireWallEntry> map = new ConcurrentHashMap<>();

    public static void add(String ip, String algorithmId) {
        add0(ip, new FireWallEntry(algorithmId));
    }

    static void add0(String ip, FireWallEntry entry) {
        map.put(ip, entry);
    }

    public static boolean isBlocked(InetSocketAddress address) {
        return map.containsKey(address.getAddress().getHostAddress());
    }

    public static void initialize() throws IOException {
        file.load();
    }

    public static void fastSave() {
        try {
            file.saveKeyAndVal(map, "|");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void onAsyncSave() {
        try {
            file.saveKeyAndVal(map, "|");
            AlixCommonMain.debug("Successfully saved the firewall.txt file!");
        } catch (IOException e) {
            e.printStackTrace();
            AlixCommonMain.logWarning("Could not save the firewall.txt file! Some information could be lost!");
        }
    }

    private FireWallManager() {
    }
}