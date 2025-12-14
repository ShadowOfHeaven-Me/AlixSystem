package shadow.utils.main.api;

import alix.api.event.AlixEvent;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.throwable.AlixError;
import alix.api.event.types.AuthReason;
import alix.spigot.api.events.auth.SpigotUserAuthEvent;
import alix.spigot.api.users.AlixSpigotUser;
import shadow.Main;

import java.util.concurrent.ExecutorService;

import static alix.api.event.AlixEvent.ThreadSource.*;

public final class AlixEventInvoker {

    public static void callOnAuthNetty(AuthReason authReason, AlixSpigotUser user, ExecutorService nettyExecutor) {
        SpigotUserAuthEvent.callEvent(authReason, user, nettyExecutor, NETTY);
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

    public static void callOnAuthInferThread(AuthReason authReason, AlixSpigotUser user) {
        callOnAuth(authReason, user, Thread.currentThread() == Main.mainServerThread ? SYNC : ASYNC);
    }
}