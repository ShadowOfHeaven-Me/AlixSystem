package alix.api.event;

import alix.api.AlixAPI;
import alix.api.event.types.UserAuthenticateEvent;
import alix.api.event.types.UserPostLoginEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

public final class EventManager {

    private final Map<ListenerPriority, Set<EventListener>> map = new ConcurrentHashMap<>();
    private volatile EventListener[] listeners = new EventListener[0];

    @ApiStatus.Internal
    public EventManager() {
    }

    /**
     * Registers the given EventListener
     **/
    public void registerListener(EventListener listener) {
        boolean modified = this.registerNoRecalculation(listener);
        if (modified) this.recalculateListeners(1);
    }

    /**
     * Registers the given EventListeners
     **/
    public void registerListeners(EventListener... listeners) {
        boolean modified = false;
        int added = 0;

        for (EventListener listener : listeners) {
            boolean success = this.registerNoRecalculation(listener);
            modified |= success;
            if (success)
                added++;
        }

        if (modified) this.recalculateListeners(listeners.length);
    }

    /**
     * Registers the given EventListeners
     **/
    public void registerListeners(Iterable<EventListener> listeners) {
        boolean modified = false;
        int added = 0;

        for (EventListener listener : listeners) {
            boolean success = this.registerNoRecalculation(listener);
            modified |= success;
            if (success)
                added++;
        }

        if (modified) this.recalculateListeners(added);
    }

    /**
     * Unregisters the given EventListener
     **/
    public void unregisterListener(EventListener listener) {
        boolean modified = this.unregisterNoRecalculation(listener);
        if (modified) this.recalculateListeners(-1);
    }

    /**
     * Unregisters the given EventListeners
     **/
    public void unregisterListeners(EventListener... listeners) {
        boolean modified = false;
        int removed = 0;

        for (EventListener listener : listeners) {
            boolean success = this.unregisterNoRecalculation(listener);
            modified |= success;
            if (success)
                removed++;
        }

        if (modified) this.recalculateListeners(removed);
    }

    /**
     * Unregisters the given EventListeners
     **/
    public void unregisterListeners(Iterable<EventListener> listeners) {
        boolean modified = false;
        int removed = 0;

        for (EventListener listener : listeners) {
            boolean success = this.unregisterNoRecalculation(listener);
            modified |= success;
            if (success)
                removed++;
        }

        if (modified) this.recalculateListeners(removed);
    }

    /**
     * Unregisters the all currently existing EventListeners
     **/
    public void clear() {
        this.map.clear();
        synchronized (this) {
            this.listeners = new EventListener[0];
        }
    }

    //Internal registration methods, separated for lesser overhead during
    // registration/de-registration of an array of EventListeners

    private boolean registerNoRecalculation(EventListener listener) {
        return this.map.computeIfAbsent(listener.getPriority(), p -> new CopyOnWriteArraySet<>()).add(listener);
    }

    private boolean unregisterNoRecalculation(EventListener listener) {
        Set<EventListener> set = this.map.get(listener.getPriority());
        return set != null && set.remove(listener);
    }

    private void recalculateListeners(int delta) {
        synchronized (this) {
            List<EventListener> list = new ArrayList<>(listeners.length + delta);//just a decent estimate for size

            for (ListenerPriority priority : ListenerPriority.values()) {
                Set<EventListener> set = this.map.get(priority);
                if (set == null) continue;
                list.addAll(set);
            }
            this.listeners = list.toArray(new EventListener[0]);
        }
    }

    private <T extends AlixEvent> void callEvent(T event, BiConsumer<EventListener, T> consumer) {
        for (EventListener listener : this.listeners) consumer.accept(listener, event);
    }

    @ApiStatus.Internal
    public static <T extends UserAuthenticateEvent> T callOnAuth(T event) {
        AlixAPI.getAPI().getEventManager().callEvent(event, EventListener::onAuth);
        return event;
    }

    @ApiStatus.Internal
    public static void callOnPostLogin(UserPostLoginEvent event) {
        AlixAPI.getAPI().getEventManager().callEvent(event, EventListener::onPostLogin);
    }
}