package alix.spigot.api.events.auth;

import alix.api.event.EventManager;
import alix.api.event.types.UserAuthenticateEvent;
import alix.spigot.api.users.AlixSpigotUser;
import org.jetbrains.annotations.ApiStatus;

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

    @ApiStatus.Internal
    public static void callEvent(AuthReason authReason, AlixSpigotUser user, ExecutorService executor, ThreadSource source) {
        EventManager.callOnAuth(new SpigotUserAuthEvent(authReason, user, executor, source));
    }
}