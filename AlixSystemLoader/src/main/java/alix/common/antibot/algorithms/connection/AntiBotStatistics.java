package alix.common.antibot.algorithms.connection;

import alix.common.antibot.algorithms.any.PanicModeManager;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.formatter.AlixFormatter;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public final class AntiBotStatistics {

    public static final AntiBotStatistics INSTANCE = new AntiBotStatistics();

    private AntiBotStatistics() {
        for (int i = 0; i < SAMPLE_SIZE; i++)
            this.cps.offerLast(0);

        AlixScheduler.repeatAsync(this::update, 1L, TimeUnit.SECONDS);
    }

    public final int startingBlocked = this.getTotalBlocked();

    private final LongAdder currentCps = new LongAdder();
    //lastCps = new AtomicInteger();
    //private final LongAdder totalConnections = new LongAdder();

    //the connections info from the last SAMPLE_SIZE amount of seconds
    private static final int SAMPLE_SIZE = 7;
    private static final int MAX_ALLOWED_CPS = 5;
    private static final int MAX_ALLOWED_SUM = SAMPLE_SIZE * MAX_ALLOWED_CPS;
    //the total amount of connections from the last SAMPLE_SIZE amount of seconds
    private final LongAdder currentSum = new LongAdder();
    private final Deque<Integer> cps = new ConcurrentLinkedDeque<>();

    //private boolean viewed;

    public void markViewed(boolean viewed) {
        //this.viewed = viewed;
    }

    public String getFormattedStatistics() {
        int total = getTotalBlocked();
        String cpsView = FireWallManager.isOsFireWallInUse ? "&7ACPS: " : "&7CPS: ";
        return AlixFormatter.translateColors(cpsView + "&c" + getCPS() + " &7Total Blocked: &c" + total + " &7Blocked Since Start: &c" + getBlockedSinceStart(total));
    }

    private int getBlockedSinceStart(int total) {
        return total - this.startingBlocked;
    }

    private int getTotalBlocked() {
        return FireWallManager.getTotalBlocked();
    }

    //todo: proxyProtocol
    public void incrementJoins() {
        //if (viewed)
        this.currentCps.increment();

        this.panicIfNecessary();
    }

    private void panicIfNecessary() {
        int max = 60;
        if (this.isLastAvgCPSGreaterThan(max))
            PanicModeManager.activate("CPS greater than " + max + "!");
    }

    boolean isLastAvgCPSGreaterThan(long val) {
        //this.currentSum.sum() / SAMPLE_SIZE > val
        return this.currentSum.sum() > val * SAMPLE_SIZE;
    }

    private float lastAverageCPS() {
        float f = this.currentSum.sum();
        return f / SAMPLE_SIZE;
    }

    public boolean isHighTraffic() {
        return this.currentSum.sum() >= MAX_ALLOWED_SUM || this.getCPS() >= 7;
    }

    public void update() {
        int currentCps = (int) this.currentCps.sumThenReset();
        //this.lastCps.set(cps);
        this.cps.offerLast(currentCps);
        Integer polled = this.cps.pollFirst();

        int delta = currentCps - polled;
        if (delta != 0)
            this.currentSum.add(delta);

        //if (this.cpsSampleSize == SAMPLE_SIZE) {
        //} else this.cpsSampleSize++;
        //this.totalConnections.add(cps);
    }

    private int getCPS() {
        return this.cps.peekLast(); //this.lastCps.get();
    }
}