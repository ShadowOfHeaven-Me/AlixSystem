/*
package alix.common.scheduler.locks;

import sun.misc.Unsafe;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.LockSupport;

public class AlixLockImpl implements AlixLock {

    private final Deque<Thread> waiting = new ArrayDeque<>();
    private volatile boolean locked;

    @Override
    public synchronized void lock() {
        if(locked) {
            Thread t = Thread.currentThread();
            this.waiting.add(t);
            LockSupport.park(t);
        }
        this.locked = true;
    }

    @Override
    public void unlock() {
        this.locked = false;
    }
}*/
