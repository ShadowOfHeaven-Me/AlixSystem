package alix.common.antibot.algorithms.connection.types;

import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.firewall.AlgorithmId;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.file.UserFileManager;
import alix.common.utils.AlixCache;
import alix.common.utils.AlixClock;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static alix.common.antibot.firewall.AlgorithmId.B3;

public final class Name2IPAlgorithm implements ConnectionAlgorithm {

    private static final AlgorithmId ALGORITHM_ID = B3;
    private static final long WINDOW_MS = 4000;
    private static final Map<InetAddress, Bucket> MAP = AlixCache.newBuilder().expireAfterAccess(WINDOW_MS, TimeUnit.MILLISECONDS).<InetAddress, Bucket>build().asMap();

    @Override
    public boolean onJoinAttempt(String name, InetAddress ip) {
        return MAP.computeIfAbsent(ip, i -> new Bucket()).onAttempt(name, ip);
    }

    static final class Bucket {

        static final long REFILL_PERIOD = 800;//5 tokens per 4 seconds
        static final int MAX_TOKENS = 12;

        int tokens = MAX_TOKENS;
        int overrun = 0;
        long lastRefill = AlixClock.currentTimeMillis();
        //name -> expire at
        //final Object2LongMap<String> lastNames = new Object2LongOpenHashMap<>();//AlixCache.newBuilder().expireAfterWrite(WINDOWS_MS, TimeUnit.MILLISECONDS).<String, Boolean>build().asMap();
        final Map<String, Long> lastNames = new HashMap<>();

        void refillIfNecessary() {
            if (this.tokens == MAX_TOKENS)
                return;

            long now = AlixClock.currentTimeMillis();
            long passed = now - this.lastRefill;
            if (passed < REFILL_PERIOD)
                return;

            int refill = (int) (passed / REFILL_PERIOD);

            this.tokens = Math.min(this.tokens + refill, MAX_TOKENS);

            if (this.tokens == MAX_TOKENS)
                this.lastRefill = now;
            else
                this.lastRefill += refill * REFILL_PERIOD;//account for rounding errors by lying a bit about lastRefill time

            if (this.tokens > 0)
                this.overrun = 0;
        }

        void removeStale() {
            long now = AlixClock.currentTimeMillis();
            this.lastNames.entrySet().removeIf(entry -> entry.getValue() < now);
        }

        synchronized boolean onAttempt(String name, InetAddress ip) {
            //already firewalled, fast-path
            if (this.tokens < 0 && this.overrun > 1)
                return true;

            this.refillIfNecessary();
            this.removeStale();
            long now = AlixClock.currentTimeMillis();

            boolean newName = this.lastNames.put(name, now + WINDOW_MS) == null;

            int penalty = 1;

            if (newName)
                penalty += UserFileManager.hasName(name) ? 1 : 2;

            int attemptCount = this.lastNames.size();
            if (attemptCount >= 2)
                penalty += Math.min(attemptCount - 1, 3);

            if (GeoIPTracker.isMapped(ip))
                penalty = Math.max(1, penalty - 1);

            this.tokens -= penalty;

            if (this.tokens < 0) {
                if (++this.overrun > 1) {
                    FireWallManager.add(ip, ALGORITHM_ID, true);
                    return true;
                }
            } else
                this.overrun = 0;

            return false;
        }
    }
}