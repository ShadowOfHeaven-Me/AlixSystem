package alix.api.event;

import alix.api.event.types.UserAuthenticateEvent;
import alix.api.event.types.UserPostLoginEvent;

public interface EventListener {

    void onPostLogin(UserPostLoginEvent event);

    void onAuth(UserAuthenticateEvent event);

    ListenerPriority getPriority();

}