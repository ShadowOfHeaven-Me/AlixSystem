package alix.common.antibot.algorithms.connection.types;

import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.firewall.AlgorithmId;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.utils.AlixCache;
import alix.common.utils.AlixClock;
import alix.common.utils.config.ConfigParams;

import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static alix.common.antibot.firewall.AlgorithmId.C2;

public final class JoinCounterAlgorithm implements ConnectionAlgorithm {

    private static final AlgorithmId ALGORITHM_ID = C2;
    private static final long WINDOW_MS = 60_000L;
    private static final Map<InetAddress, IpStats> MAP = AlixCache.newBuilder().expireAfterAccess(WINDOW_MS, TimeUnit.MILLISECONDS).<InetAddress, IpStats>build().asMap();

    @Override
    public boolean onJoinAttempt(String name, InetAddress ip) {
        return MAP.computeIfAbsent(ip, n -> new IpStats()).newAttempt(name, ip);
    }

    static final class IpStats {
        final Deque<Entry> attempts = new ArrayDeque<>();
        int violationScore = 0;
        //name -> tried to connect X times
        final Map<String, Integer> nameCounts = new HashMap<>();
        //final Set<String> recentNames = AlixCache.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).<String, Object>build().asMap().keySet();

        void expungeStale() {
            long now = AlixClock.currentTimeMillis();
            Entry entry;
            while ((entry = this.attempts.peekFirst()) != null && entry.expireAt < now) {
                this.attempts.pollFirst();
                this.violationScore -= entry.score;
                this.nameCounts.compute(entry.name, (n, c) -> c == null || c <= 1 ? null : c - 1);
            }
        }

        static final int MAX_NAMES =
                Math.max(ConfigParams.maximumTotalAccounts, 12),
                MAX_JOIN_ATTEMPTS = MAX_NAMES * 5;//a safe 5 attempts per name?

        synchronized boolean newAttempt(String name, InetAddress ip) {
            if (this.violationScore > 100)
                return true;

            this.expungeStale();

            long now = AlixClock.currentTimeMillis();

            boolean newName = this.nameCounts.merge(name, 1, Integer::sum) == 1;
            boolean ipMapped = GeoIPTracker.isMapped(ip);

            int score = 0;

            if (!ipMapped) {
                score += newName ? (this.nameCounts.size() == 1 ? 3 : 8) : 1;
            }

            score += newName ? 10 : 1;

            if (this.nameCounts.size() > MAX_NAMES)
                score += 20;

            if (this.attempts.size() > MAX_JOIN_ATTEMPTS)
                score += 15;

            this.attempts.add(new Entry(name, score, now + WINDOW_MS));

            this.violationScore += score;

            int total = this.violationScore;
            if (total > 100) {
                FireWallManager.add(ip, ALGORITHM_ID, true);
                return true;
            }
            return false;
        }
    }

    record Entry(String name, int score, long expireAt) {
    }
}