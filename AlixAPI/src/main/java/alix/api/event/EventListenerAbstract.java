package alix.api.event;

import alix.api.event.types.UserAuthenticateEvent;
import alix.api.event.types.UserPostLoginEvent;

public interface EventListenerAbstract extends EventListener {

    @Override
    default void onPostLogin(UserPostLoginEvent event) {
    }

    @Override
    default void onAuth(UserAuthenticateEvent event) {
    }

    @Override
    default ListenerPriority getPriority() {
        return ListenerPriority.NORMAL;
    }
}