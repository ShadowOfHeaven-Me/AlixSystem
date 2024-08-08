package shadow.systems.virtualization.manager;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import shadow.Main;

import java.util.Collection;
import java.util.function.Function;

abstract class VirtualEventManager {

    private static final EmptyListener EMPTY_LISTENER = new EmptyListener();//a non-null instance is required
    private final VirtualizedHandlerList virtualHandlerList;

    <T extends Event> VirtualEventManager(Class<T> eventClazz, Function<VirtualEventManager, VirtualEventExecutor<T>> eventExecutorFunc) {
        this.virtualHandlerList = UserSemiVirtualization.replaceHandlers(eventClazz, eventExecutorFunc.apply(this));
    }

    final void returnOriginalHandler() {
        UserSemiVirtualization.replaceVirtualHandlerWithOriginal(PlayerQuitEvent.class, this.virtualHandlerList);
    }

/*    final void invokeOriginalListeners(Event event) {
        UserSemiVirtualization.invokeOriginalEventListeners(this.virtualHandlerList.getOriginalListeners(), event);
    }*/

    static abstract class VirtualEventExecutor<T extends Event> implements EventExecutor {

        private final VirtualEventManager eventManager;

        VirtualEventExecutor(VirtualEventManager eventManager) {
            this.eventManager = eventManager;
        }

        abstract void onInvocation(T event);

        @Override
        public final void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
            //this.eventManager.invokeOriginalListeners(event);
            this.onInvocation((T) event);
        }
    }

    static final class VirtualizedHandlerList extends HandlerList {

        final HandlerList originalHandler;
        //private final RegisteredListener[] virtualListeners;
        private final VirtualRegisteredListener virtualListener;
        private RegisteredListener[] lastCache;

        <T extends Event> VirtualizedHandlerList(HandlerList original, VirtualEventExecutor<T> eventExecutor) {
            this.originalHandler = original;
            this.virtualListener = new VirtualRegisteredListener(eventExecutor);
            this.getRegisteredListeners();//add the listener
            //this.virtualListeners = new RegisteredListener[]{new VirtualRegisteredListener(eventExecutor)};
        }

        //Fixes invalid benchmark reports
        @SuppressWarnings("ArrayEquality")
        @NotNull
        @Override
        public RegisteredListener[] getRegisteredListeners() {
            RegisteredListener[] current = this.originalHandler.getRegisteredListeners();

            if (current == lastCache) return current;

            RegisteredListener[] full = new RegisteredListener[current.length + 1];
            System.arraycopy(current, 0, full, 0, current.length);
            full[current.length] = this.virtualListener;

            this.lastCache = full;
            return full;
        }

/*        private RegisteredListener[] getOriginalListeners() {
            return this.originalHandler.getRegisteredListeners();
        }*/

        @Override
        public void register(@NotNull RegisteredListener listener) {
            this.originalHandler.register(listener);
        }

        @Override
        public void registerAll(@NotNull Collection<RegisteredListener> listeners) {
            this.originalHandler.registerAll(listeners);
        }

        @Override
        public void unregister(@NotNull RegisteredListener listener) {
            this.originalHandler.unregister(listener);
        }

        @Override
        public void unregister(@NotNull Plugin plugin) {
            this.originalHandler.unregister(plugin);
        }

        @Override
        public void unregister(@NotNull Listener listener) {
            this.originalHandler.unregister(listener);
        }

        @Override
        public void bake() {
            this.originalHandler.bake();
        }
    }

    private static final class VirtualRegisteredListener extends RegisteredListener {

        private VirtualRegisteredListener(VirtualEventExecutor executor) {
            super(EMPTY_LISTENER, executor, EventPriority.MONITOR, Main.plugin, false);
        }
    }

    private static final class EmptyListener implements Listener {
    }
}