package shadow.systems.virtualization.manager;

import org.bukkit.event.player.PlayerJoinEvent;
import shadow.utils.users.Verifications;
import shadow.utils.users.types.UnverifiedUser;

final class JoinEventManager extends VirtualEventManager {

    JoinEventManager() {
        super(PlayerJoinEvent.class, JoinEventExecutor::new);
    }

    static final class JoinEventExecutor extends VirtualEventExecutor<PlayerJoinEvent> {

        @Override
        void onInvocation(PlayerJoinEvent event) {
            //var uuid = event.getPlayer().getUniqueId();
            //Main.debug("JOIN EVENT UUID= " + uuid + " ver=" + uuid.version());
            UnverifiedUser user = Verifications.get(event.getPlayer());
            if (user != null) {//stop the event's side effects
                user.originalJoinMessage = event.getJoinMessage();
                //user.getPacketBlocker().getFallPhase().
                event.setJoinMessage(null);
            } //else this.eventManager.invokeOriginalListeners(event);
        }
    }
}