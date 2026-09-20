package alix.common.antibot.algorithms.any;

import alix.common.connection.filters.GeoIPTracker;
import alix.common.scheduler.AlixScheduler;
import ua.nanit.limbo.server.Log;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PanicModeManager {

    private static final AtomicBoolean PANIC_MODE = new AtomicBoolean();
    //private static final boolean UNSAFE = IPUtils.hasUnsafeImpl();
    //private static final ThreadLocal<Inet4Address> INET4_CACHE = UNSAFE ? ThreadLocal.withInitial(IPUtils::allocV4WithHolder) : null;

    public static boolean activate(String reason) {
        if (!PANIC_MODE.compareAndSet(false, true)) return false;

        Log.warning(reason + " Activating panic mode! Only non-suspicious IPs may connect!");

        AlixScheduler.runLaterAsync(() -> {
            deactivate("2 minutes have passed!");
        }, 2, TimeUnit.MINUTES);
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

    public static boolean isV4Blocked(int addr) {
        return isActive() && !GeoIPTracker.isv4Mapped(addr);
    }
}