package alix.common.scheduler.impl.bukkit;

import alix.common.scheduler.impl.AbstractAlixScheduler;
import alix.common.scheduler.tasks.SchedulerTask;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.annotation.AlixIntrinsified;
import alix.common.utils.other.annotation.ScheduledForFix;
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
     * PaperAlixScheduler is a class that is used exclusively for Paper servers and it's forks.
     * It functions mainly thanks to the AlixTaskList class. For further information, see
     * the explanation below. It's faster at the cost of executing the given task
     * always at the end of a tick, making the changes only visible at the next tick.
     * It also extends the AbstractAlixScheduler, which schedules the task using the
     * ScheduledThreadPoolExecutor, which relies on the contract that each tick is 50 ms.
     * This it not the case when the server is lagging - then the tasks will be scheduled
     * faster (in respect to the main thread). Thus, this implementation should not be used
     * for tasks that rely on accurate tick delay and need to be executed synchronously.
     *
     * @author ShadowOfHeaven
     **/

    private final AlixTaskList executeNow = new AlixTaskList();

    @AlixIntrinsified(method = "Bukkit.getScheduler")
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
         * Here's a thorough explanation of how this task list works:
         * First we declare two LinkedAlixTask variables:
         * <p>
         * first - The temporary holder of the first task,
         * having all the other task nodes linked to it;
         * <p>
         * last - The temporary holder responsible for linking
         * further task nodes;
         * </p>
         **/

        //private final LoopList<Runnable> tasks = LoopList.newConcurrentOfSize(40);
        private volatile LinkedAlixTask first, last;
        //private volatile boolean executeExtraNextTick;


        /**
         * The 'synchronized' keyword is used instead of a ReentrantLock,
         * because it was proven to be more efficient with a small amount
         * of threads waiting - This is the case here, as the method itself takes
         * very little time to finish
         **/

        private void add(Runnable task) {
            /*int index = this.tasks.getAndUpdate(i -> i != this.tasks.size() ? i + 1 : i);//ranges from 0 to size(), with size() indicating not enough memory
            if (index != this.tasks.size()) {
                this.tasks.setValue(index, task);
            } else {//we ran out of storage space
                synchronized (this) {
                    if (this.first == null) this.first = this.last = new LinkedAlixTask(task);
                    else this.last = this.last.next = new LinkedAlixTask(task);
                }
            }*/
            synchronized (this) {
                if (this.first == null) this.first = this.last = new LinkedAlixTask(task);
                else this.last = this.last.next = new LinkedAlixTask(task);
            }
        }

        @ScheduledForFix
        private void executeAllAndClear() {
            LinkedAlixTask task = this.first;
            if (task == null) return;

            synchronized (this) {//this synchronization is fine, since the tasks are executed at the end of a tick and the operations are all extremely lightweight
                this.first = this.last = null;//all of the task nodes are still held by the 'task' variable
            }

            do {
                try {
                    task.task.run();//execute the tasks synchronously
                } catch (Exception e) {
                    AlixCommonUtils.logException(e);
                }
            } while ((task = task.next) != null);
            /*if (executeExtraNextTick) this.execExtra();

            int size = this.tasks.getAndSetCurrentIndex(0);//to ensure thread safety, set it to 0 right after reading
            if (size <= 0) return;

            boolean outOfSpace = size == this.tasks.size();

            this.tasks.drain(outOfSpace ? size - 1 : size, task -> {
                try {
                    task.run();//execute the tasks synchronously
                } catch (Exception e) {
                    AlixCommonUtils.logException(e);
                }
            });

            if (!outOfSpace) return;

            //for extra safety, leave this check here
            if (this.first == null) {
                this.executeExtraNextTick = true;
                return;//get the current run, and return if there is none to execute
            }
            this.execExtra();*/
        }

/*        private void execExtra() {
            LinkedAlixTask task = this.first;

            //for extra safety, leave this check here
            if (task == null) {
                this.executeExtraNextTick = true;
                return;//get the current run, and return if there is none to execute
            }
            this.executeExtraNextTick = false;

            synchronized (this) {//this synchronization is fine, since the tasks are executed at the end of a tick and the operations are all extremely lightweight
                this.first = this.last = null;//all of the task nodes are still held by the 'task' variable
            }

            do {
                try {
                    task.task.run();//execute the tasks synchronously
                } catch (Exception e) {
                    AlixCommonUtils.logException(e);
                }
            } while ((task = task.next) != null);
        }
    }*/

        private static final class LinkedAlixTask {

            private final Runnable task;
            private volatile LinkedAlixTask next;

            private LinkedAlixTask(Runnable task) {
                this.task = task;
            }
        }
    }
}