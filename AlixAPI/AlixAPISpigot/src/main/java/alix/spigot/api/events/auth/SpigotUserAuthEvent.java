package alix.spigot.api.events.auth;

import alix.api.event.EventManager;
import alix.api.event.types.AuthReason;
import alix.api.event.types.UserAuthenticateEvent;
import alix.spigot.api.events.AbstractAlixEvent;
import alix.spigot.api.users.AlixSpigotUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

public class SpigotUserAuthEvent extends AbstractAlixEvent implements UserAuthenticateEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final AuthReason authReason;

    private SpigotUserAuthEvent(AuthReason authReason, AlixSpigotUser user, ExecutorService executor, ThreadSource source) {
        super(user, executor, source);
        this.authReason = authReason;
    }

    @Override
    public AlixSpigotUser getUser() {
        return (AlixSpigotUser) super.getUser();
    }

    @Override
    public AuthReason getAuthReason() {
        return authReason;
    }

    @ApiStatus.Internal
    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @ApiStatus.Internal
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @ApiStatus.Internal
    public static void callEvent(AuthReason authReason, AlixSpigotUser user, ExecutorService executor, ThreadSource source) {
        var event = EventManager.callOnAuth(new SpigotUserAuthEvent(authReason, user, executor, source));
        AbstractAlixEvent.onEventCall(event);
    }
}