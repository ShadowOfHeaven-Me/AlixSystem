package shadow.systems.virtualization.manager;

import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import shadow.Main;
import shadow.systems.login.result.LoginVerdictManager;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;

final class SpawnLocEventManager extends VirtualEventManager {

    SpawnLocEventManager() {
        super(PlayerSpawnLocationEvent.class, SpawnEventExecutor::new);
    }

    private static final class SpawnEventExecutor extends VirtualEventExecutor<PlayerSpawnLocationEvent> {

        private final AlixMessage joinVerified = Messages.getAsObject("log-player-join-auto-verified");

        SpawnEventExecutor(VirtualEventManager eventManager) {
            super(eventManager);
        }

        @Override
        void onInvocation(PlayerSpawnLocationEvent event) {
            //Main.logError("SPAWN LOC EVENT");
            Player player = event.getPlayer();
            Location joinLoc = event.getSpawnLocation();

            TemporaryUser tempUser = LoginVerdictManager.get(player);
            UnverifiedUser user = AlixHandler.handleVirtualPlayerJoin(player, tempUser);

            boolean verified = user == null;

            if (verified) Main.logInfo(this.joinVerified.format(player.getName(), tempUser.getLoginInfo().getIP(), tempUser.getLoginInfo().getVerdict().readableName()));

            //the join location is in the captcha world
            if (joinLoc.getWorld().getUID().equals(AlixWorld.CAPTCHA_WORLD.getUID())) {
                //if (verified) this.eventManager.invokeOriginalListeners(event);
                if (!verified) user.originalSpawnEventLocation = joinLoc;
                event.setSpawnLocation(verified ? OriginalLocationsManager.getOriginalLocation(player) : AlixWorld.TELEPORT_LOCATION);//ensure it's at the correct location
                return;
            }

            if (verified) {
                //this.eventManager.invokeOriginalListeners(event);
                return;//the player doesn't need to be teleported for verification
            }

            //The join location was not in the captcha world

            //Further handling helps in saving the actual original location, even if dead
            Location originLoc = player.isDead() ? player.getBedSpawnLocation() : joinLoc;//if dead set to their spawn, original otherwise

            //Check whether the original location is valid
            if (originLoc != null && !originLoc.getWorld().getUID().equals(AlixWorld.CAPTCHA_WORLD.getUID()))
                OriginalLocationsManager.add(player, originLoc);//remember the original spawn location

            user.originalSpawnEventLocation = joinLoc;
            event.setSpawnLocation(AlixWorld.TELEPORT_LOCATION);//set the captcha world location as the spawn location (a faster onJoin teleport alternative)
        }
    }
}