package alix.api;

import alix.api.event.EventManager;
import alix.api.settings.AlixSettings;
import org.jetbrains.annotations.NotNull;

public final class AlixAPI {

    private final EventManager eventManager = new EventManager();
    private final AlixSettings settings = new AlixSettings();

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

    @NotNull
    public AlixSettings getSettings() {
        return settings;
    }

    private static final class Holder {
        private static final AlixAPI API = new AlixAPI();
    }
}