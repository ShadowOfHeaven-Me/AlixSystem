package alix.spigot.api.events.listener;

import alix.api.event.EventListenerAbstract;
import alix.api.event.types.UserAuthenticateEvent;
import alix.spigot.api.events.SpigotUserAuthEvent;

public abstract class SpigotEventListener implements EventListenerAbstract {

    //https://api.github.com/users/ShadowOfHeaven-Me

    @Override
    public final void onAuth(UserAuthenticateEvent event) {
        this.onAuth0((SpigotUserAuthEvent) event);
    }

    public abstract void onAuth0(SpigotUserAuthEvent event);
}