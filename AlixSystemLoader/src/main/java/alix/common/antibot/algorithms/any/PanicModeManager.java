package alix.common.antibot.algorithms.any;

import alix.common.antibot.ip.IPUtils;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.annotation.OptimizationCandidate;
import ua.nanit.limbo.server.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PanicModeManager {

    private static final AtomicBoolean PANIC_MODE = new AtomicBoolean();
    private static final boolean UNSAFE = IPUtils.hasUnsafeImpl();
    private static final ThreadLocal<Inet4Address> INET4_CACHE = UNSAFE ? ThreadLocal.withInitial(IPUtils::allocV4WithHolder) : null;

    public static boolean activate(String reason) {
        if (!PANIC_MODE.compareAndSet(false, true)) return false;

        Log.warning(reason + " Activating panic mode! Only mapped IPs may connect!");

        AlixScheduler.runLaterAsync(() -> {
            deactivate("10 minutes have passed!");
        }, 10, TimeUnit.MINUTES);
        return true;
    }

    public static boolean deactivate(String reason) {
        if (!PANIC_MODE.compareAndSet(true, false)) return false;

        Log.warning(reason + " Disabling panic mode! All IPs may now connect!");
        return true;
    }

    public static boolean isActive() {
        return PANIC_MODE.get();
    }

    public static boolean isBlocked(InetAddress addr) {
        return isActive() && !GeoIPTracker.isMapped(addr);
    }

    //no need to alloc an Inet4Address, especially after I made a segfault here...
    @OptimizationCandidate
    public static boolean isV4Blocked(int addr) {
        if (!isActive()) return false;

        Inet4Address obj;

        if (UNSAFE) {
            obj = INET4_CACHE.get();
            IPUtils.override(obj, addr);
        } else {
            try {
                obj = (Inet4Address) InetAddress.getByAddress(IPUtils.ipv4ByteArray(addr));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        return !GeoIPTracker.isMapped(obj);
    }
}