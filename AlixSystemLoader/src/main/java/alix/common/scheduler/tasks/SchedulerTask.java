package alix.common.scheduler.tasks;

@FunctionalInterface
public interface SchedulerTask {

    void cancel();

}