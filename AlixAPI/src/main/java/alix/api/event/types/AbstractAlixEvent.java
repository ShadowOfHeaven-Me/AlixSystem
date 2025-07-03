package alix.api.event.types;

import alix.api.event.AlixEvent;
import alix.api.user.AlixCommonUser;

import java.util.concurrent.ExecutorService;

abstract class AbstractAlixEvent implements AlixEvent {

    private final AlixCommonUser user;
    private final ExecutorService executor;
    private final ThreadSource source;

    AbstractAlixEvent(AlixCommonUser user, ExecutorService executor, ThreadSource source) {
        this.user = user;
        this.executor = executor;
        this.source = source;
    }

    @Override
    public AlixCommonUser getUser() {
        return user;
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public ThreadSource getThreadSource() {
        return source;
    }
}