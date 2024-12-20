package alix.common.scheduler.runnables;

import alix.common.utils.collections.queue.ConcurrentAlixDeque;

public final class AlixThread extends Thread {

    private static final ConcurrentAlixDeque<AlixThread> alixThreads = new ConcurrentAlixDeque<>();
    private final Runnable command;
    private final long millisDelay;
    private volatile boolean running = true;

    public AlixThread(Runnable command, long millisDelay, String name) {
        super("AlixSystem-" + name);
        this.command = command;
        this.millisDelay = millisDelay;
        alixThreads.offerLast(this);
        this.start();
    }

    /**
     * @noinspection BusyWait <- IntelliJ got confused Ig
     */
    @Override
    public void run() {
        while (running) {
            long time = System.currentTimeMillis();

            this.command.run();

            long elapsed = System.currentTimeMillis() - time;
            long diff = this.millisDelay - elapsed;

            if (diff > 0) {
                try {
                    Thread.sleep(diff);//non-zero sleep time
                } catch (InterruptedException e) {
                    this.interrupt();
                }
            }//else the command execution took more than the delay - do nothing
        }
    }

    public static void shutdownAllAlixThreads() {
        alixThreads.forEach(AlixThread::shutdown);
        alixThreads.clear();
    }

    private void shutdown() {
        this.running = false;
        this.interrupt();
    }
}