package shadow.systems.virtualization.manager;

import org.bukkit.event.player.PlayerJoinEvent;
import shadow.utils.users.Verifications;
import shadow.utils.users.types.UnverifiedUser;

final class JoinEventManager extends VirtualEventManager {

    JoinEventManager() {
        super(PlayerJoinEvent.class, JoinEventExecutor::new);
    }

    private static final class JoinEventExecutor extends VirtualEventExecutor<PlayerJoinEvent> {

        JoinEventExecutor(VirtualEventManager eventManager) {
            super(eventManager);
        }

        @Override
        void onInvocation(PlayerJoinEvent event) {
            //Main.logError("JOIN LOC EVENT");
            UnverifiedUser user = Verifications.get(event.getPlayer());
            if (user != null) {//stop the event's invocation
                user.originalJoinMessage = event.getJoinMessage();
                //user.getPacketBlocker().getFallPhase().
                event.setJoinMessage(null);
            } //else this.eventManager.invokeOriginalListeners(event);
        }
    }
}