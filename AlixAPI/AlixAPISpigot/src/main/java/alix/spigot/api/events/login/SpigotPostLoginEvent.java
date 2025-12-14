package alix.spigot.api.events.login;

import alix.api.event.types.UserPostLoginEvent;
import alix.spigot.api.events.AbstractAlixEvent;
import alix.spigot.api.users.AbstractSpigotUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

public class SpigotPostLoginEvent extends AbstractAlixEvent implements UserPostLoginEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final boolean isPremium;

    public SpigotPostLoginEvent(boolean isPremium, AbstractSpigotUser user, ExecutorService executor, ThreadSource source) {
        super(user, executor, source);
        this.isPremium = isPremium;
    }

    @Override
    public boolean isPremium() {
        return this.isPremium;
    }

    @Override
    public AbstractSpigotUser getUser() {
        return (AbstractSpigotUser) super.getUser();
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
}