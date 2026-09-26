package alix.common.antibot.algorithms.any;

import alix.common.connection.filters.GeoIPTracker;
import alix.common.scheduler.AlixScheduler;
import ua.nanit.limbo.server.Log;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class PanicModeManager {

    private static final AtomicBoolean PANIC_MODE = new AtomicBoolean();
    //Separate from PANIC_MODE above: this is a persistent opt-out an admin can flip via '/as panicmode
    //disable', not a transient state. Unlike deactivate() (which only ever turns an ALREADY-active panic
    //mode back off, and does nothing to stop it triggering again a moment later - manually, or via
    //AntiBotStatistics#panicIfNecessary()'s own CPS-based auto-trigger), this blocks activate() from
    //succeeding at all until re-enabled, for a server that doesn't want this behavior available.
    private static final AtomicBoolean LOCKED = new AtomicBoolean();
    //Each activate() stamps its own timer with the current generation and only deactivates if nothing has
    //re-activated since - otherwise an off->on cycle within the 2-minute auto-deactivation window left the
    //FIRST activation's stale timer pending, and it could turn off a later, unrelated activation early.
    private static final AtomicInteger GENERATION = new AtomicInteger();
    //private static final boolean UNSAFE = IPUtils.hasUnsafeImpl();
    //private static final ThreadLocal<Inet4Address> INET4_CACHE = UNSAFE ? ThreadLocal.withInitial(IPUtils::allocV4WithHolder) : null;

    //FUNCTIONALITY (audit, 2026-09-24): LOCKED and PANIC_MODE used to be two independent AtomicBooleans,
    //each individually check-then-act (activate() read LOCKED, then separately CAS'd PANIC_MODE) - fine
    //under a single caller, but panicIfNecessary() calls activate() from EVERY connection once CPS exceeds
    //the threshold (AntiBotStatistics#panicIfNecessary()), so a real attack has many concurrent activate()
    //calls in flight at exactly the moment an admin is likely to run '/as panicmode disable'. That let this
    //interleaving occur: activate() reads LOCKED as still false, THEN lock() completes (locks + deactivates
    //what was active), THEN the first activate()'s now-stale check resumes and CASes PANIC_MODE back to
    //true anyway - panic mode re-activating moments after the admin was told it was locked, during the
    //exact live-attack scenario the emergency disable exists for. Synchronizing the whole check-and-act
    //bodies closes that window; the critical section is a couple of field reads/writes plus a log call, all
    //cheap, and only actually contended while CPS is already above the panic threshold.
    private static final Object LOCK = new Object();

    public static boolean activate(String reason) {
        synchronized (LOCK) {
            if (LOCKED.get()) return false;
            if (!PANIC_MODE.compareAndSet(false, true)) return false;

            Log.warning(reason + " Activating panic mode! Only non-suspicious IPs may connect!");

            int generation = GENERATION.incrementAndGet();
            AlixScheduler.runLaterAsync(() -> {
                if (GENERATION.get() == generation)
                    deactivate("2 minutes have passed!");
            }, 2, TimeUnit.MINUTES);
            return true;
        }
    }

    public static boolean deactivate(String reason) {
        synchronized (LOCK) {
            if (!PANIC_MODE.compareAndSet(true, false)) return false;

            Log.warning(reason + " Disabling panic mode! All IPs may now connect!");
            return true;
        }
    }

    public static boolean lock(String reason) {
        synchronized (LOCK) {
            if (!LOCKED.compareAndSet(false, true)) return false;

            deactivate(reason);
            Log.warning(reason + " Panic mode has been disabled - it will not trigger again, manually or automatically, until re-enabled.");
            return true;
        }
    }

    public static boolean unlock(String reason) {
        synchronized (LOCK) {
            if (!LOCKED.compareAndSet(true, false)) return false;

            Log.warning(reason + " Panic mode has been re-enabled.");
            return true;
        }
    }

    public static boolean isLocked() {
        return LOCKED.get();
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