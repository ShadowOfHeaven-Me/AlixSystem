package shadow.systems.virtualization.manager;

import org.bukkit.event.player.PlayerQuitEvent;
import shadow.utils.main.AlixHandler;

final class QuitEventManager extends VirtualEventManager {

    QuitEventManager() {
        super(PlayerQuitEvent.class, QuitEventExecutor::new);
    }

    static final class QuitEventExecutor extends VirtualEventExecutor<PlayerQuitEvent> {

        @Override
        void onInvocation(PlayerQuitEvent event) {
            //Main.logInfo("ON QUIT EVENT INVOCATION: ");
            AlixHandler.handleVirtualPlayerQuit(event);
            //invoke the original event listeners if the user quitting is verified
            //if (!AlixHandler.handleVirtualPlayerQuit(event)) this.eventManager.invokeOriginalListeners(event);
        }
    }
}