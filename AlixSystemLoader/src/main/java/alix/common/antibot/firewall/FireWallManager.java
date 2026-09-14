package alix.common.antibot.firewall;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.adaptive.AdaptiveAnomalyDetector;
import alix.common.antibot.algorithms.any.PanicModeManager;
import alix.common.antibot.firewall.ataraxia.AlixAtaraxia;
import alix.common.antibot.firewall.entry.FireWallEntry;
import alix.common.antibot.ip.IPUtils;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.AlixMathUtils;
import alix.common.utils.collections.fastutil.ConcurrentInt62Set;
import alix.common.utils.collections.fastutil.InetAddressMap;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public final class FireWallManager {

    //File
    private static final FireWallFile file = new FireWallFile();

    //Built-in
    private static final RoaringBitmap staticIpv4Tree = new RoaringBitmap();
    private static final Set<InetAddress> staticIpv6Set = ConcurrentHashMap.newKeySet();

    //Dynamic
    private static final Map<InetAddress, FireWallEntry> dynamicMap = new ConcurrentHashMap<>();
    private static final ConcurrentInt62Set dynamicIpv4SetFastLookUp = new ConcurrentInt62Set(1 << 14);

    public static void addCauseException(InetSocketAddress ip, Throwable t) {
        addCauseException(ip.getAddress(), t);
    }

    public static final String EXCEPTION_CAUGHT_KEY = "ex_ca: ";
    public static final Duration EXCEPTION_TIMEOUT = Duration.of(30, TimeUnit.MINUTES.toChronoUnit());
    public static final long
            NO_TIMEOUT = 0,
            ATARAXIA_TIMEOUT = 30 * 60 * 1000L;//30m

    private static final AlixMessage antiAbuseConsoleMessage = Messages.getAsObject("anti-abuse-fail-console-message");
    private static final String firewallLogsExhaustedConsoleMessage = Messages.get("firewall-exhausted-logs");

    private static final LongAdder recentFirewalls = new LongAdder();
    private static final int MAX_RECENT_FIREWALLS = 8;
    private static final long RECENT_FIREWALL_WINDOW = 20_000L;
    private static final AtomicBoolean MESSAGES_LOCKED = new AtomicBoolean();
    private static final AtomicLong LAST_FIREWALL = new AtomicLong();

    public static void addDynamic(List<InetAddress> list) {
        list.forEach(ip -> addDynamic(ip, "Ataraxia", ATARAXIA_TIMEOUT));
    }

    public static void addCauseException(InetAddress ip, Throwable t) {
        addCauseException(ip, t, EXCEPTION_TIMEOUT.getSeconds());
    }

    public static void addCauseException(InetAddress ip, Throwable t, long timeoutInSeconds) {
        addDynamic(ip, t.getMessage(), timeoutInSeconds);
    }

    public static void addDynamic(InetAddress ip, String message, long timeoutInSeconds) {
        boolean added = null == add(ip, EXCEPTION_CAUGHT_KEY + message, timeoutInSeconds, TimeUnit.SECONDS);
        if (!added || checkIfLogsExhausted()) return;

        String expires = timeoutInSeconds > 0 ? "in " + AlixCommonUtils.prettyTime(timeoutInSeconds) : "Never";
        AlixCommonMain.logInfo(antiAbuseConsoleMessage.format(ip.getHostAddress(), message, expires));
    }

    private static boolean checkIfLogsExhausted() {
        long now = System.currentTimeMillis();
        if (now - LAST_FIREWALL.get() > RECENT_FIREWALL_WINDOW && MESSAGES_LOCKED.get())
            MESSAGES_LOCKED.set(false);

        LAST_FIREWALL.set(now);

        if (MESSAGES_LOCKED.get())
            return true;

        recentFirewalls.increment();
        int sum = (int) recentFirewalls.sum();

        AlixScheduler.runLaterAsync(recentFirewalls::decrement, RECENT_FIREWALL_WINDOW, TimeUnit.SECONDS);

        if (sum > MAX_RECENT_FIREWALLS && MESSAGES_LOCKED.compareAndSet(false, true)) {
            AlixCommonMain.logInfo(firewallLogsExhaustedConsoleMessage);
            return true;
        }
        return false;
    }

    private static final AlixMessage antiBotConsoleMessage = Messages.getAsObject("anti-bot-fail-console-message");

    public static boolean add(InetAddress ip, AlgorithmId algorithmId, boolean log) {
        return add(ip, algorithmId, log, NO_TIMEOUT, TimeUnit.SECONDS);
    }

    public static boolean add(InetAddress ip, AlgorithmId algorithmId, boolean log, long timeoutIn, TimeUnit unit) {
        boolean added = add(ip, algorithmId.name(), timeoutIn, unit) == null;
        if (log && added && !checkIfLogsExhausted())
            AlixCommonMain.logInfo(antiBotConsoleMessage.format(ip.getHostAddress(), algorithmId));

        return added;
    }

    static FireWallEntry add(InetAddress ip, String message, long timeoutIn, TimeUnit unit) {
        long timeoutAt = timeoutIn <= 0 ? 0 : System.currentTimeMillis() + unit.toMillis(timeoutIn);
        return add0(ip, FireWallEntry.from(message, timeoutAt), false);
    }

    static FireWallEntry add0(InetAddress ip, FireWallEntry entry, boolean loaded) {
        AlixAtaraxia.blacklist(ip);

        // Static
        if (entry == FireWallEntry.BUILT_IN) {
            if (ip instanceof Inet4Address ipv4) {
                staticIpv4Tree.add(IPUtils.ipv4Value(ipv4));
            } else {
                staticIpv6Set.add(ip);
            }
            return entry;
        }

        if (!loaded)
            AdaptiveAnomalyDetector.onFirewall();
        // Dynamic
        FireWallEntry previous = dynamicMap.putIfAbsent(ip, entry);
        if (ip instanceof Inet4Address ipv4) {
            long val = Integer.toUnsignedLong(IPUtils.ipv4Value(ipv4));
            dynamicIpv4SetFastLookUp.add(val);

            if (entry.timeoutAt() > 0) {
                long timeoutIn = entry.timeoutAt() - System.currentTimeMillis();
                if (timeoutIn > 0)
                    AlixScheduler.runLaterAsync(() -> removeDynamic0(ip), timeoutIn, TimeUnit.MILLISECONDS);
                else
                    removeDynamic0(ip);
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

    public static void removeDynamic(List<InetAddress> list) {
        list.forEach(FireWallManager::removeDynamic);
    }

    public static boolean removeDynamic(InetAddress ip) {
        return removeDynamic0(ip);
    }

    private static boolean removeDynamic0(InetAddress ip) {
        boolean removed = dynamicMap.remove(ip) != null;
        if (ip instanceof Inet4Address ipv4)
            dynamicIpv4SetFastLookUp.remove(Integer.toUnsignedLong(IPUtils.ipv4Value(ipv4)));

        return removed;
    }

    public static boolean isV4Blocked0(int ipv4Value) {
        return staticIpv4Tree.contains(ipv4Value)
               || dynamicIpv4SetFastLookUp.contains(Integer.toUnsignedLong(ipv4Value))
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

    public static Set<InetAddress> staticBlockedSet() {
        Set<InetAddress> keys = new HashSet<>(staticBlocked());
        staticIpv4Tree.stream().forEach(k -> keys.add(InetAddressMap.intToInet4Address(k)));
        keys.addAll(staticIpv6Set);
        return keys;
    }

    public static Set<InetAddress> dynamicBlockedSet() {
        return dynamicMap.keySet();
    }

    public static int getTotalBlocked() {
        return staticBlocked() + dynamicBlocked();
    }

    private static void save0() throws IOException {
        file.saveKeyAndVal(dynamicMap, FireWallEntry.DELIMITER, FireWallEntry::shouldSave, InetAddress::getHostAddress, null);
    }

    public static final CompletableFuture<Void> AT_LOAD_COMPLETE = new CompletableFuture<>();

    public static void init() {
        AlixScheduler.async(() -> {
            if (ConfigParams.loadBuiltInIps) loadWithBuiltIn0();
            else loadWithoutBuiltIn0();

            AT_LOAD_COMPLETE.complete(null);
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
            AlixFileManager.readLines(is, ip -> add0(IPUtils.fromAddress(ip), FireWallEntry.BUILT_IN, true), false);

            int builtIn = staticBlocked();
            file.load();
            int total = getTotalBlocked();

            staticIpv4Tree.runOptimize();

            //not accounting for object overhead in ipv6
            long bytes = staticIpv4Tree.getLongSizeInBytes() + staticIpv6Set.size() * 16L;
            float MB = AlixMathUtils.round(bytes / 1e6f, 1);

            AlixCommonMain.logInfo("Fully loaded the FireWall DataBase. Loaded built-in blacklisted IPs: " + AlixCommonUtils.formatNicely(builtIn) +
                                   " (~" + MB + " MB), Blacklisted by this server: " + AlixCommonUtils.formatNicely(total - builtIn) +
                                   ", Total: " + AlixCommonUtils.formatNicely(total));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private FireWallManager() {
    }
}