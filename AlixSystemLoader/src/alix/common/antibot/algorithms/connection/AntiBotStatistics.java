package alix.common.antibot.algorithms.connection;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.utils.formatter.AlixFormatter;

import java.util.concurrent.atomic.AtomicInteger;

public final class AntiBotStatistics {

    public static final AntiBotStatistics INSTANCE = new AntiBotStatistics();

    private AntiBotStatistics() {
    }

    public final int startingBlocked = this.getTotalBlocked();

    private final AtomicInteger
            currentCps = new AtomicInteger(),
            lastCps = new AtomicInteger(),
            totalConnections = new AtomicInteger();

    public String getFormattedStatistics() {
        int total = getTotalBlocked();
        return AlixFormatter.format("&7CPS: &c" + getCPS() + " &7Total Blocked: &c" + total + " &7Blocked Since Start: &c" + getBlockedSinceStart(total));
    }

    private int getBlockedSinceStart(int total) {
        return total - startingBlocked;
    }

    private int getTotalBlocked() {
        return FireWallManager.getMap().size();
    }

    public void incrementJoins() {
        this.currentCps.getAndIncrement();
    }

    public void reset() {
        this.lastCps.set(this.currentCps.get());
        this.totalConnections.getAndAdd(this.currentCps.get());
        this.currentCps.set(0);
    }

    private int getCPS() {
        return this.lastCps.get();
    }
}