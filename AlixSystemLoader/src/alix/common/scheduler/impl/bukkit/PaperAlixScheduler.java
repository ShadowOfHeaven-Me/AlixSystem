package alix.common.scheduler.impl.bukkit;

import alix.common.scheduler.impl.AbstractAlixScheduler;
import alix.common.scheduler.tasks.SchedulerTask;
import alix.common.utils.AlixCommonUtils;
import alix.loaders.bukkit.BukkitAlixMain;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class PaperAlixScheduler extends AbstractAlixScheduler {

    private final AlixTaskList executeNow = new AlixTaskList();

    public PaperAlixScheduler() {
        Bukkit.getPluginManager().registerEvents(new PaperSyncTickEvent(), BukkitAlixMain.instance);
    }

    @Override
    public void sync(Runnable r) {
        this.executeNow.add(r);
    }

    @Override
    public SchedulerTask runLaterSync(Runnable r, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = this.poolExecutor.schedule(() -> sync(r), delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public SchedulerTask repeatSync(Runnable r, long delay, TimeUnit unit) {
        long tickDelay = unit.toMillis(delay) / 50;
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(BukkitAlixMain.instance, r, tickDelay, tickDelay);
        return task::cancel;
    }

    private final class PaperSyncTickEvent implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onTickEnd(ServerTickEndEvent event) {
            executeNow.executeAllAndClear();
        }
    }

    private final class AlixTaskList {

        private volatile LinkedAlixTask first, last;

        //the 'synchronized' keyword was used instead of a ReentrantLock
        //because it was proven to be more efficient with a small amount
        //of threads waiting - This is the case here, as the method itself takes
        //very little time to finish
        private synchronized void add(Runnable task) {//sync because of multi-thread access
            if (this.first == null) this.first = this.last = new LinkedAlixTask(task);
            else this.last = this.last.next = new LinkedAlixTask(task);
        }

        //not synchronized for the sake of speed with an extremely unlikely task miss chance
        private void executeAllAndClear() { //has an extremely low chance of missing a task execution - the new task object addition would need to take longer adding than the execution of the first task, making it pretty much impossible
            LinkedAlixTask task = this.first;

            if (task == null) return;

            //no need for locks, because the node object itself is updated at task addition
            //get the first node, as all of the other nodes are attached to it
            this.first = this.last = null; //clear the deque to prevent elements from being added as the tasks are being executed

            //execute the tasks synchronously
            do {
                try {
                    task.task.run();//run the task
                    task.executed = true;//set as executed to let the GC remove it freely
                } catch (Exception e) {
                    AlixCommonUtils.logException(e);
                }
            } while ((task = task.next) != null);
        }
    }

    private final class LinkedAlixTask {

        private final Runnable task;
        private LinkedAlixTask next;
        private boolean executed;

        private LinkedAlixTask(Runnable task) {
            this.task = task;
        }

        @Override
        protected void finalize() throws Throwable {
            if (!this.executed)
                executeNow.add(this.task);//reschedule the task if it was ever lost (really low probability)
            super.finalize();
        }
    }
}