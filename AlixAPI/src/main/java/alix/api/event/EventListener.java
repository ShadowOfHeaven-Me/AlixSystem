package alix.api.event;

import alix.api.event.types.UserAuthenticateEvent;

public interface EventListener {

    void onAuth(UserAuthenticateEvent event);

    ListenerPriority getPriority();

}