package alix.spigot.api.events;

import alix.api.AlixAPI;
import alix.api.event.AlixEvent;
import alix.api.user.AlixCommonUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.concurrent.ExecutorService;

public abstract class AbstractAlixEvent extends Event implements AlixEvent {

    private final AlixCommonUser user;
    private final ExecutorService executor;
    private final ThreadSource source;

    protected AbstractAlixEvent(AlixCommonUser user, ExecutorService executor, ThreadSource source) {
        super(source.isAsync());
        this.user = user;
        this.executor = executor;
        this.source = source;
    }

    @Override
    public AlixCommonUser getUser() {
        return user;
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public ThreadSource getThreadSource() {
        return source;
    }

    protected static void onEventCall(AbstractAlixEvent event) {
        if (AlixAPI.getAPI().getSettings().invokeStdEventListeners())
            Bukkit.getPluginManager().callEvent(event);
    }
}