package alix.common.scheduler.impl.bukkit;

import alix.loaders.bukkit.BukkitAlixMain;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import alix.common.scheduler.impl.AbstractAlixScheduler;
import alix.common.scheduler.tasks.SchedulerTask;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class BukkitAlixScheduler extends AbstractAlixScheduler {

    private final BukkitScheduler sync;

    public BukkitAlixScheduler() {
        this.sync = Bukkit.getScheduler();
    }

    @Override
    public void sync(Runnable r) {
        this.sync.scheduleSyncDelayedTask(BukkitAlixMain.instance, r);
    }

    @Override
    public SchedulerTask runLaterSync(Runnable r, long delay, TimeUnit unit) {
        BukkitTask task = this.sync.runTaskLater(BukkitAlixMain.instance, r, unit.toMillis(delay) / 50);
        return task::cancel;
    }

    @Override
    public SchedulerTask repeatSync(Runnable r, long interval, TimeUnit unit) {
        long intervalInTicks = unit.toMillis(interval) / 50;//cannot convert to seconds because of conversion losses
        BukkitTask task = this.sync.runTaskTimer(BukkitAlixMain.instance, r, intervalInTicks, intervalInTicks);
        return task::cancel;
    }
}