package alix.common.scheduler.impl.bukkit;

import alix.common.reflection.BukkitReflection;
import alix.common.reflection.CommonReflection;
import alix.common.scheduler.impl.AbstractAlixScheduler;
import alix.common.scheduler.tasks.SchedulerTask;
import alix.loaders.bukkit.BukkitAlixMain;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class BukkitAlixScheduler extends AbstractAlixScheduler {

    private final SyncScheduler sync;

    public BukkitAlixScheduler() {
        Field f = CommonReflection.getFieldOrNullIfAbsent(BukkitReflection.MC_SERVER_OBJ, "processQueue");
        this.sync = f != null ? new NMSSyncScheduler(f) : new BukkitSyncScheduler();
        /*if (f == null)
            AlixCommonMain.logInfo("Using the unoptimized BukkitAlixScheduler for task execution - Paper is suggested for better performance.");
        else
            AlixCommonMain.logInfo("Using the optimized NMSAlixScheduler for task execution.");*/
    }

    @Override
    public void sync(Runnable r) {
        this.sync.runSync(r);
    }

    @Override
    public SchedulerTask runLaterSync(Runnable r, long delay, TimeUnit unit) {
        return this.sync.runSyncLater(r, delay, unit);
    }

    @Override
    public SchedulerTask repeatSync(Runnable r, long interval, TimeUnit unit) {
        long intervalInTicks = unit.toMillis(interval) / 50;//cannot convert to seconds because of conversion losses
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(BukkitAlixMain.instance, r, intervalInTicks, intervalInTicks);
        return task::cancel;
    }

    private interface SyncScheduler {

        void runSync(Runnable cmd);

        SchedulerTask runSyncLater(Runnable cmd, long delay, TimeUnit unit);

    }

    private final class NMSSyncScheduler implements SyncScheduler {

        private final Queue<Runnable> tasks;

        private NMSSyncScheduler(Field f) {
            this.tasks = (Queue<Runnable>) CommonReflection.get(f, BukkitReflection.MC_SERVER_OBJ);
        }

        @Override
        public void runSync(Runnable command) {
            this.tasks.offer(command);
        }

        @Override
        public SchedulerTask runSyncLater(Runnable cmd, long delay, TimeUnit unit) {
            ScheduledFuture<?> future = poolExecutor.schedule(() -> runSync(cmd), delay, unit);
            return () -> future.cancel(false);
        }
    }

    private static final class BukkitSyncScheduler implements SyncScheduler {

        private final BukkitScheduler sync;

        private BukkitSyncScheduler() {
            this.sync = Bukkit.getScheduler();
        }

        @Override
        public void runSync(Runnable command) {
            this.sync.scheduleSyncDelayedTask(BukkitAlixMain.instance, command);
        }

        @Override
        public SchedulerTask runSyncLater(Runnable cmd, long delay, TimeUnit unit) {
            BukkitTask task = this.sync.runTaskLater(BukkitAlixMain.instance, cmd, unit.toMillis(delay) / 50);
            return task::cancel;
        }
    }
}