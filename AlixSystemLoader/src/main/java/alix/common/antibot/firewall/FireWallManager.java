package alix.common.antibot.firewall;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.any.PanicModeManager;
import alix.common.antibot.firewall.entry.FireWallEntry;
import alix.common.antibot.ip.IPUtils;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.fastutil.ConcurrentInt62Set;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.file.AlixFileManager;
import alix.common.utils.other.throwable.AlixException;
import lombok.SneakyThrows;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class FireWallManager {

    //Linux IpSet
    private static final AlixOSFireWall osFireWall = AlixOSFireWall.INSTANCE;
    public static final boolean isOsFireWallInUse = AlixOSFireWall.isOsFireWallInUse;

    //File
    private static final FireWallFile file = new FireWallFile();

    //Built-in
    private static final RoaringBitmap staticIpv4Tree = new RoaringBitmap();
    private static final Set<InetAddress> staticIpv6Set = ConcurrentHashMap.newKeySet();

    //Dynamic
    private static final Map<InetAddress, FireWallEntry> dynamicMap = new ConcurrentHashMap<>();
    private static final ConcurrentInt62Set dynamicIpv4Set = new ConcurrentInt62Set(1 << 14);

    public static void addCauseException(InetSocketAddress ip, Throwable t) {
        addCauseException(ip.getAddress(), t);
    }

    public static final String EXCEPTION_CAUGHT_KEY = "ex_ca: ";
    public static final Duration EXCEPTION_TIMEOUT = Duration.of(30, TimeUnit.MINUTES.toChronoUnit());
    public static final long NO_TIMEOUT = 0;

    public static void addCauseException(InetAddress ip, Throwable t) {
        addCauseException(ip, t, EXCEPTION_TIMEOUT.getSeconds());
    }

    public static void addCauseException(InetAddress ip, Throwable t, long timeoutInSeconds) {
        add(ip, EXCEPTION_CAUGHT_KEY + t.getMessage(), timeoutInSeconds, TimeUnit.SECONDS);
    }

    private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-message");

    public static boolean add(InetAddress ip, AlgorithmId algorithmId, boolean log) {
        return add(ip, algorithmId, log, NO_TIMEOUT, TimeUnit.SECONDS);
    }

    public static boolean add(InetAddress ip, AlgorithmId algorithmId, boolean log, long timeoutIn, TimeUnit unit) {
        boolean added = add(ip, algorithmId.name(), timeoutIn, unit) == null;
        if (log && added)
            AlixCommonMain.logInfo(consoleMessage.format(ip.getHostAddress(), algorithmId));

        return added;
    }

    private static void osBlacklist0(String ip) {
        try {
            osFireWall.blacklist(ip);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static FireWallEntry add(InetAddress ip, String message, long timeoutIn, TimeUnit unit) {
        long timeoutAt = System.currentTimeMillis() + unit.toMillis(timeoutIn);
        return add0(ip, FireWallEntry.from(message, timeoutAt));
    }

    static FireWallEntry add0(InetAddress ip, FireWallEntry entry) {
        if (isOsFireWallInUse)
            osBlacklist0(ip.getHostAddress());

        // Static
        if (entry == FireWallEntry.BUILT_IN) {
            if (ip instanceof Inet4Address ipv4) {
                staticIpv4Tree.add(IPUtils.ipv4Value(ipv4));
            } else {
                staticIpv6Set.add(ip);
            }
            return null;
        }

        // Dynamic
        FireWallEntry previous = dynamicMap.put(ip, entry);
        if (ip instanceof Inet4Address ipv4) {
            long val = Integer.toUnsignedLong(IPUtils.ipv4Value(ipv4));
            dynamicIpv4Set.add(val);

            if (entry.timeoutAt() > 0) {
                long timeoutIn = entry.timeoutAt() - System.currentTimeMillis();
                AlixScheduler.runLaterAsync(() -> removeDynamic0(ip), timeoutIn, TimeUnit.MILLISECONDS);
            }
        }
        return previous;
    }

    public static boolean isBlocked(InetSocketAddress address) {
        return isBlocked0(address.getAddress());
    }

    public static boolean isBlocked0(InetAddress address) {
        if (address instanceof Inet4Address ipv4) {
            int ipVal = IPUtils.ipv4Value(ipv4);
            if (staticIpv4Tree.contains(ipVal)) return true;
        } else if (staticIpv6Set.contains(address)) {
            return true;
        }

        // Optimistic read
        FireWallEntry entry = dynamicMap.get(address);
        if (entry != null) {
            long timeoutAt = entry.timeoutAt();
            if (timeoutAt > 0 && System.currentTimeMillis() > timeoutAt) {
                // Expired. Lazily remove
                removeDynamic0(address);
                return PanicModeManager.isBlocked(address);
            }
            return true;
        }

        return PanicModeManager.isBlocked(address);
    }

    private static void removeDynamic0(InetAddress ip) {
        dynamicMap.remove(ip);
        if (ip instanceof Inet4Address ipv4) {
            dynamicIpv4Set.remove(Integer.toUnsignedLong(IPUtils.ipv4Value(ipv4)));
        }
    }

    public static boolean isV4Blocked0(int ipv4Value) {
        return staticIpv4Tree.contains(ipv4Value)
               || dynamicIpv4Set.contains(Integer.toUnsignedLong(ipv4Value))
               || PanicModeManager.isV4Blocked(ipv4Value);
    }

    public static void fastSave() {
        try {
            save0();
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

    public static int staticBlocked() {
        return staticIpv4Tree.getCardinality() + staticIpv6Set.size();
    }

    public static int dynamicBlocked() {
        return dynamicMap.size();
    }

    public static int getTotalBlocked() {
        return staticBlocked() + dynamicBlocked();
    }

    private static void save0() throws IOException {
        file.saveKeyAndVal(dynamicMap, FireWallEntry.DELIMITER, FireWallEntry::shouldSave, InetAddress::getHostAddress, null);
    }

    public static void init() {
        AlixScheduler.async(() -> {
            if (ConfigParams.loadBuiltInIps) loadWithBuiltIn0();
            else loadWithoutBuiltIn0();

            staticIpv4Tree.runOptimize();
        });
    }

    @SneakyThrows
    private static void loadWithoutBuiltIn0() {
        file.load();
        int total = getTotalBlocked();
        AlixCommonMain.logInfo("Partially loaded the FireWall DataBase (built-in loading disabled). Total: " + total);
    }

    private static void loadWithBuiltIn0() {
        try (var is = FireWallManager.class.getResourceAsStream("files/bad_ips.txt")) {
            AlixFileManager.readLines(is, ip -> add0(IPUtils.fromAddress(ip), FireWallEntry.BUILT_IN), false);
            int builtIn = staticBlocked();
            file.load();
            int total = getTotalBlocked();
            AlixCommonMain.logInfo("Fully loaded the FireWall DataBase. Loaded built-in blacklisted IPs: " + builtIn + ", Blacklisted by this server: " + (total - builtIn) + ", Total: " + total);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private FireWallManager() {
    }
}