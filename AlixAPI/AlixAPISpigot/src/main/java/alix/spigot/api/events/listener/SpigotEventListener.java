package alix.spigot.api.events.listener;

import alix.api.event.EventListenerAbstract;
import alix.api.event.types.UserAuthenticateEvent;
import alix.api.event.types.UserPostLoginEvent;
import alix.spigot.api.events.auth.SpigotUserAuthEvent;
import alix.spigot.api.events.login.SpigotPostLoginEvent;

public abstract class SpigotEventListener implements EventListenerAbstract {

    //https://api.github.com/users/ShadowOfHeaven-Me

    @Override
    public final void onAuth(UserAuthenticateEvent event) {
        this.onAuth0((SpigotUserAuthEvent) event);
    }

    @Override
    public final void onPostLogin(UserPostLoginEvent event) {
        this.onPostLogin0((SpigotPostLoginEvent) event);
    }

    public abstract void onAuth0(SpigotUserAuthEvent event);

    public abstract void onPostLogin0(SpigotPostLoginEvent event);
}