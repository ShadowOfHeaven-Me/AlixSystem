package shadow.systems.virtualization.manager;

import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import org.bukkit.event.player.PlayerJoinEvent;
import shadow.Main;
import shadow.systems.login.result.LoginVerdictManager;
import shadow.utils.main.AlixHandler;
import shadow.utils.users.types.UnverifiedUser;

final class JoinEventManager extends VirtualEventManager {

    JoinEventManager() {
        super(PlayerJoinEvent.class, JoinEventExecutor::new);
    }

    static final class JoinEventExecutor extends VirtualEventExecutor<PlayerJoinEvent> {

        private final AlixMessage joinVerified = Messages.getAsObject("log-player-join-auto-verified");

        @Override
        void onInvocation(PlayerJoinEvent event) {
            var player = event.getPlayer();
            var tempUser = LoginVerdictManager.getExisting(player);
            if (tempUser == null)
                return;

            UnverifiedUser user = AlixHandler.handleVirtualPlayerJoin(player, tempUser); //Verifications.get(event.getPlayer());
            if (user != null) {//stop the event's side effects
                user.originalJoinMessage = event.getJoinMessage();
                event.setJoinMessage(null);
            } else {
                Main.logInfo(this.joinVerified.format(player.getName(), tempUser.getLoginInfo().getTextIP(), tempUser.getLoginInfo().getVerdict().readableName()));
            }
        }
    }
}