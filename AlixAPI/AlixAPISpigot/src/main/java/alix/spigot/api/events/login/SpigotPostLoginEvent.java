package alix.spigot.api.events.login;

import alix.api.event.types.UserPostLoginEvent;
import alix.spigot.api.users.AbstractSpigotUser;

import java.util.concurrent.ExecutorService;

public class SpigotPostLoginEvent extends UserPostLoginEvent {

    public SpigotPostLoginEvent(boolean isPremium, AbstractSpigotUser user, ExecutorService executor, ThreadSource source) {
        super(isPremium, user, executor, source);
    }

    @Override
    public AbstractSpigotUser getUser() {
        return (AbstractSpigotUser) super.getUser();
    }
}