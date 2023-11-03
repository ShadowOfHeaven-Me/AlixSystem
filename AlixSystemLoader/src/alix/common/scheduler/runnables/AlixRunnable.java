/*
package alix.common.scheduler.runnables;

import alix.common.scheduler.tasks.SchedulerTask;

import java.util.concurrent.TimeUnit;

public abstract class AlixRunnable implements Runnable {

    private final SchedulerTask task;

    public AlixRunnable(long interval, TimeUnit unit, boolean async) {
        this.task = async ? JavaScheduler.repeatAsync(this, interval, unit) : JavaScheduler.repeatSync(this, interval, unit);
    }

    public void cancel() {
        task.cancel();
    }
}*/
