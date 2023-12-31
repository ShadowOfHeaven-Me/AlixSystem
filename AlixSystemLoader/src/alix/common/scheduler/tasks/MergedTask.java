package alix.common.scheduler.tasks;

import alix.common.scheduler.AlixScheduler;

public final class MergedTask {

    private Runnable task;

    public void setTask(Runnable task) {
        this.task = task;
    }

    public void mergeOrSet(Runnable task) {
        if (this.task == null) this.task = task;
        else {
            Runnable t2 = this.task;
            this.task = () -> {
                t2.run();
                task.run();
            };
        }
    }

    public void executeSyncAndRemove() {
        if (task == null) return;
        AlixScheduler.sync(this.task);
        this.task = null;
    }
}