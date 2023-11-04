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

    /**
     * PaperAlixScheduler is a class that is used exclusively for Paper servers or it's forks.
     * It functions mainly thanks to the AlixTaskList class. For further information, see
     * the explanation below. It's faster at the cost of executing the given task
     * always a tick (or two) later than when invoked. It also extends the AbstractAlixScheduler,
     * which schedules the task using the ScheduledThreadPoolExecutor, which relies
     * on the contract that each tick is 50 ms - This it not the case when the server
     * is lagging. The tasks then will be scheduled faster. Thus, this implementation
     * should not be used for tasks that rely on accurate tick delay and need
     * to be executed synchronously.
     *
     * @author ShadowOfHeaven
     **/

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

    private static final class AlixTaskList {

        /**
         * Here's a thorough explanation of how this task list works.
         * First we declare three LinkedAlixTask variables:
         * <p>
         * firstTemp - is the temporary holder of the first task,
         * having all the other task nodes linked to it;
         * <p>
         * lastTemp - is the temporary holder responsible for linking
         * further task nodes;
         * <p>
         * nextRun - is the task node that will be run in
         * the server next tick;
         *
         * </p>
         **/

        private volatile LinkedAlixTask firstTemp, lastTemp, nextRun;

        /**
         * The 'synchronized' keyword is used instead of a ReentrantLock,
         * because it was proven to be more efficient with a small amount
         * of threads waiting - This is the case here, as the method itself takes
         * very little time to finish
         **/

        private synchronized void add(Runnable task) {
            if (this.firstTemp == null) this.firstTemp = this.lastTemp = new LinkedAlixTask(task);
            else this.lastTemp = this.lastTemp.next = new LinkedAlixTask(task);
        }


        //not synchronized because the nextRun variable is only assigned in this method
        private void executeAllAndClear() {
            LinkedAlixTask task = this.nextRun;//get the current run

            if (task != null) {//execute if there is a task
                do {
                    try {
                        task.task.run();//execute the tasks synchronously
                    } catch (Exception e) {
                        AlixCommonUtils.logException(e);
                    }
                } while ((task = task.next) != null);
            }

            this.nextRun = this.firstTemp;//assign the next run (possibly a null)

            if (this.firstTemp != null && this.firstTemp == this.nextRun)
                this.firstTemp = this.lastTemp = null;//only set it to null when it's not a null, and it's the very same task that was assigned moments before, in order to make sure that no tasks are ever missed
        }
    }

    private static final class LinkedAlixTask {

        private final Runnable task;
        private LinkedAlixTask next;

        private LinkedAlixTask(Runnable task) {
            this.task = task;
        }
    }
}