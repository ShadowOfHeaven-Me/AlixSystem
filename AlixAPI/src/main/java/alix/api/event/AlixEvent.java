package alix.api.event;

import alix.api.user.AlixCommonUser;

import java.util.concurrent.ExecutorService;

public interface AlixEvent {

    /**
     * @return The user associated with this event
     **/
    AlixCommonUser getUser();

    /**
     * @return The executor used to invoke this event
     **/
    ExecutorService getExecutor();

    /**
     * @return The thread source of this event, associated with the {@link AlixEvent#getExecutor() getExecutor()} method
     **/
    ThreadSource getThreadSource();

    enum ThreadSource {

        /**
         * Invoked on the server's main thread (spigot and forks)
         **/
        SYNC,
        /**
         * Invoked async
         **/
        ASYNC,
        /**
         * Invoked async on a thread designed for blocking operations (a virtual thread on java 21+)
         **/
        ASYNC_BLOCKING,
        /**
         * Invoked on one of netty's event loop threads
         **/
        NETTY;

        public boolean isAsync() {
            return this != SYNC;
        }
    }
}