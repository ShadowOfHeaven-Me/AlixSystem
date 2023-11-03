/*
package alix.common.scheduler.locks;

public class SingleThreadedAlixLockImpl implements AlixLock {//primitive and unoptimized lock class

    //private final Object lock = new Object();
    private volatile boolean locked = false;

    private SingleThreadedAlixLockImpl() {

    }

    */
/** @noinspection StatementWithEmptyBody*//*

    public void lock() {
        while (locked);//waitIfLocked();//waits before locking it again - makes the thread wait (cannot use Object#wait due to some minecraft tick mechanics - this is however fine, as the tasks executed between lock and unlock are very fast)
        this.locked = true;
    }

    public void unlock() {
        //if (!locked) return;


        //lock.notifyAll();//only one thread is waiting because it's either single-threaded or has the synchronized keyword used
        this.locked = false;
    }

*/
/*    private void waitIfLocked() {
        while (locked) {
*//*
*/
/*            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }*//*
*/
/*
        }
    }*//*


    private static final class MultiThreadedAlixLockPartialSyncImpl extends SingleThreadedAlixLockImpl {

        @Override
        public void lock() {
            synchronized (this) {//multi-thread access
                super.lock();
            }
        }
    }

    private static final class MultiThreadedAlixLockFullSyncImpl extends SingleThreadedAlixLockImpl {

        @Override
        public void lock() {
            synchronized (this) {//multi-thread access
                super.lock();
            }
        }

        @Override
        public void unlock() {
            synchronized (this) {
                super.unlock();
            }
        }
    }

    public static SingleThreadedAlixLockImpl createSingleThreadedLockImpl() {
        return new SingleThreadedAlixLockImpl();
    }

    public static SingleThreadedAlixLockImpl createMultiThreadedLockImpl(boolean syncUnlock) {
        return syncUnlock ? new MultiThreadedAlixLockFullSyncImpl() : new MultiThreadedAlixLockPartialSyncImpl();
    }
}*/
