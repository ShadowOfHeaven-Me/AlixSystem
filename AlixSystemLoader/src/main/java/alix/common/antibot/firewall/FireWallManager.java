package alix.common.antibot.firewall;

import alix.common.AlixCommonMain;
import alix.common.antibot.ip.IPUtils;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.fastutil.ConcurrentInt62Set;
import alix.common.utils.file.AlixFileManager;
import alix.common.utils.other.throwable.AlixException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FireWallManager {

    //Disabled for now
    private static final AlixOSFireWall osFireWall = AlixOSFireWall.INSTANCE;//AlixOSFireWall.INSTANCE;
    public static final boolean isOsFireWallInUse = AlixOSFireWall.isOsFireWallInUse;
    private static final FireWallFile file = new FireWallFile();
    private static final Map<InetAddress, FireWallEntry> map = new ConcurrentHashMap<>(1 << 19);//524288
    private static final ConcurrentInt62Set intIpv4Set = FireWallType.isIPv4FastLookUpEnabled() ? new ConcurrentInt62Set(1 << 19) : null;
    //private static final IntOpenHashSet intIpv4Set = FireWallType.isIPv4FastLookUpEnabled() ? new IntOpenHashSet(1 << 19) : null;

    public static void addCauseException(InetSocketAddress ip, Throwable t) {
        addCauseException(ip.getAddress(), t);
    }

    public static void addCauseException(InetAddress ip, Throwable t) {
        add0(ip, new FireWallEntry("ex_ca: " + t.getMessage()));
    }

    public static boolean add(InetAddress ip, String algorithmId) {
        return add0(ip, new FireWallEntry(algorithmId)) == null;
    }

    private static void osBlacklist0(String ip) {
        try {
            osFireWall.blacklist(ip);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static FireWallEntry add0(InetAddress ip, FireWallEntry entry) {
        if (isOsFireWallInUse) osBlacklist0(ip.getHostAddress());
        FireWallEntry previous = map.put(ip, entry);
        if (intIpv4Set != null && previous == null && ip.getClass() == Inet4Address.class)
            intIpv4Set.add(Integer.toUnsignedLong(IPUtils.ipv4Value((Inet4Address) ip)));
        return previous;
    }

    public static boolean isBlocked(InetSocketAddress address) {
        return map.containsKey(address.getAddress());
    }

    public static boolean isBlocked(InetAddress address) {
        return map.containsKey(address);
    }

    public static boolean isBlocked(long ipv4Value) {
        return intIpv4Set.contains(ipv4Value);
    }

    public static void fastSave() {
        try {
            //AlixCommonMain.logError("Started saving firewall.txt...");
            //long t = System.nanoTime();
            save0();
            //AlixCommonMain.logError("Took " + (System.nanoTime() - t) / Math.pow(10, 6) + "ms too save the firewall file!");
        } catch (IOException e) {
            throw new AlixException(e);
        }
    }

    public static void onAsyncSave() {
        try {
            save0();
            AlixCommonMain.debug("Successfully saved the firewall.txt file!");
        } catch (IOException e) {
            e.printStackTrace();
            AlixCommonMain.logWarning("Could not save the firewall.txt file! Some information could be lost!");
        }
    }

    private static void save0() throws IOException {
        file.saveKeyAndVal(map, "|", FireWallEntry::isNotBuiltIn, InetAddress::getHostAddress, null);
    }

    public static Map<InetAddress, FireWallEntry> getMap() {
        return map;
    }

    public static void init() {
        AlixScheduler.async(() -> {
            try (InputStream is = FireWallManager.class.getResourceAsStream("files/bad_ips.txt")) {
                AlixFileManager.readLines(is, ip -> add0(IPUtils.fromAddress(ip), new FireWallEntry(null)), false);
                int builtIn = map.size();
                file.load();
                int total = map.size();
                AlixCommonMain.logInfo("Fully loaded the FireWall DataBase. Built-in blacklisted IPs: " + builtIn + ", Blacklisted by this server: " + (total - builtIn) + ", Total: " + total);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    private FireWallManager() {
    }
}