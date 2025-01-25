package alix.api.event;

import alix.api.event.types.UserAuthenticateEvent;

public interface EventListenerAbstract extends EventListener {

    @Override
    default void onAuth(UserAuthenticateEvent event) {
    }

    @Override
    default ListenerPriority getPriority() {
        return ListenerPriority.NORMAL;
    }
}