package shadow.utils.main.api;

import alix.api.event.AlixEvent;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.throwable.AlixError;
import alix.spigot.api.events.AuthReason;
import alix.spigot.api.events.SpigotUserAuthEvent;
import alix.spigot.api.users.AlixSpigotUser;

import java.util.concurrent.ExecutorService;

public final class AlixEventInvoker {

    public static void callOnAuthNetty(AuthReason authReason, AlixSpigotUser user, ExecutorService nettyExecutor) {
        SpigotUserAuthEvent.callEvent(authReason, user, nettyExecutor, AlixEvent.ThreadSource.NETTY);
    }

    private static ExecutorService fromSource(AlixEvent.ThreadSource source) {
        switch (source) {
            case SYNC:
                return AlixScheduler.getSyncExecutor();
            case ASYNC:
                return AlixScheduler.getAsyncExecutor();
            case ASYNC_BLOCKING:
                return AlixScheduler.getAsyncBlockingExecutor();
            case NETTY:
                throw new AlixError("AlixEvent.ThreadSource " + source + " requires EventLoop as parameter!");
            default:
                throw new AlixError();
        }
    }

    public static void callOnAuth(AuthReason authReason, AlixSpigotUser user, AlixEvent.ThreadSource source) {
        SpigotUserAuthEvent.callEvent(authReason, user, fromSource(source), source);
    }
}