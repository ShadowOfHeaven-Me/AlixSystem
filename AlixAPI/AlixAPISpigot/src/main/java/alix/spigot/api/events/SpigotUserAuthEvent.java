package alix.spigot.api.events;

import alix.api.event.types.UserAuthenticateEvent;
import alix.spigot.api.users.AlixSpigotUser;

import java.util.concurrent.ExecutorService;

public class SpigotUserAuthEvent extends UserAuthenticateEvent {

    private final AuthReason authReason;

    private SpigotUserAuthEvent(AuthReason authReason, AlixSpigotUser user, ExecutorService executor, ThreadSource source) {
        super(user, executor, source);
        this.authReason = authReason;
    }

    @Override
    public AlixSpigotUser getUser() {
        return (AlixSpigotUser) super.getUser();
    }

    public AuthReason getAuthReason() {
        return authReason;
    }

    public static void callEvent(AuthReason authReason, AlixSpigotUser user, ExecutorService executor, ThreadSource source) {
        new SpigotUserAuthEvent(authReason, user, executor, source).callEvent();
    }
}