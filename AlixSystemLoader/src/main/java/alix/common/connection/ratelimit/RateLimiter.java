package alix.common.connection.ratelimit;

import alix.common.utils.AlixClock;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class RateLimiter<K> {

    //per subnet:
    //ATTACK -> max 10 new connections per 3 seconds
    //ELEVATED ->

    private final Map<K, Deque<ConnectionEntry>> map;

    private final int maxConnections;
    private final long timeInMillis;

    public RateLimiter(int maxConnections, int timeInSeconds) {
        this.maxConnections = maxConnections;
        this.timeInMillis = timeInSeconds * 1000L;

        this.map = new ConcurrentHashMap<>(); //AlixCache.newBuilder().expireAfterAccess(timeInSeconds, TimeUnit.SECONDS).<K, Deque<ConnectionEntry>>build().asMap();
    }

    //this is not guaranteed to be atomic, but that's fine
    public boolean tryAcquire(K key) {
        long now = AlixClock.currentTimeMillis();
        var deque = this.map.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        //remove stale
        ConnectionEntry entry;
        while ((entry = deque.peekFirst()) != null && entry.expiresAt() < now)
            deque.pollFirst();

        //it's fine to do this, size is small
        int size = deque.size();
        if (size >= this.maxConnections) return false;

        deque.offerLast(new ConnectionEntry(now + this.timeInMillis));
        return true;
    }
}