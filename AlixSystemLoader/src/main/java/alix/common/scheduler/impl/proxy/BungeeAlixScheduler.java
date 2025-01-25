package alix.common.scheduler.impl.proxy;

import alix.common.scheduler.impl.AbstractAlixScheduler;
import alix.common.scheduler.tasks.SchedulerTask;

import java.util.concurrent.TimeUnit;

public final class BungeeAlixScheduler extends AbstractAlixScheduler {

    //private final TaskScheduler scheduler = ProxyServer.getInstance().getScheduler();

    @Override
    public void sync(Runnable r) {
        throw new UnsupportedOperationException("sync on BungeeCord is unsupported!");
    }

    @Override
    public SchedulerTask runLaterSync(Runnable r, long d, TimeUnit u) {
        throw new UnsupportedOperationException("runLaterSync on BungeeCord is unsupported!");
    }

    @Override
    public SchedulerTask repeatSync(Runnable r, long d, TimeUnit u) {
        throw new UnsupportedOperationException("repeatSync on BungeeCord is unsupported!");
    }
}