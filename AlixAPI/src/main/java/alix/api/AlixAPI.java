package alix.api;

import alix.api.event.EventManager;
import org.jetbrains.annotations.NotNull;

public final class AlixAPI {

    private final EventManager eventManager = new EventManager();

    private AlixAPI() {
    }

    @NotNull
    public static AlixAPI getAPI() {
        return Holder.API;
    }

    @NotNull
    public EventManager getEventManager() {
        return eventManager;
    }

    private static final class Holder {
        private static final AlixAPI API = new AlixAPI();
    }
}