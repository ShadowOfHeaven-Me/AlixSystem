package shadow.systems.virtualization.manager;

import org.bukkit.Location;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import shadow.systems.login.result.LoginVerdictManager;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.world.AlixWorld;

import java.util.UUID;
import java.util.function.Consumer;

final class SpawnLocEventManager extends VirtualEventManager {

    @SuppressWarnings("removal")
    SpawnLocEventManager() {
        super(PlayerSpawnLocationEvent.class, SpawnEventExecutor::new);
    }

    //get new join loc, null if shouldn't be set
    public static Location getJoinLoc(UUID uuid, Location joinLoc, boolean verified) {
        TemporaryUser tempUser = LoginVerdictManager.get(uuid);

        if (tempUser == null || AlixUtils.isFakeChannel(tempUser.getChannel()))
            return null;

        //the join location is in the captcha world
        if (joinLoc.getWorld().equals(AlixWorld.CAPTCHA_WORLD)) {
            //if (verified) this.eventManager.invokeOriginalListeners(event);
            if (verified) {
                //event.setSpawnLocation(OriginalLocationsManager.getOriginalLocation(player));//tp back if the user is verified but in the captcha world
                return OriginalLocationsManager.getOriginalLocation(uuid);
            }
            //user.originalSpawnEventLocation = joinLoc;
            //OriginalLocationsManager.add(uuid, joinLoc);
            //event.setSpawnLocation(AlixWorld.TELEPORT_LOCATION);//ensure it's at the correct location
            return AlixWorld.TELEPORT_LOCATION;
        }

        if (verified) {
            //this.eventManager.invokeOriginalListeners(event);
            return null;//the player doesn't need to be teleported for verification
        }

        //The join location was not in the captcha world

        //Further handling helps in saving the actual original location, even if dead
        //Bukkit.getOfflinePlayerIfCached("sex").ban()
        //Location originLoc = player.isDead() ? player.getBedSpawnLocation() : joinLoc;//if dead set to their spawn, original otherwise

        OriginalLocationsManager.add(uuid, joinLoc);
        //event.setSpawnLocation(AlixWorld.TELEPORT_LOCATION);//ensure it's at the correct location
        return AlixWorld.TELEPORT_LOCATION;
    }

    static void onSpawnLocEvent(UUID uuid, Location joinLoc, Consumer<Location> setLocation) {
        AlixUser user = UserManager.get(uuid);
        if (user == null)
            return;

        Location newLoc = getJoinLoc(uuid, joinLoc, user.isVerified());
        if (newLoc != null)
            setLocation.accept(newLoc);
    }

    @SuppressWarnings("removal")
    static final class SpawnEventExecutor extends VirtualEventExecutor<PlayerSpawnLocationEvent> {

        SpawnEventExecutor() {
            super();
        }

        @SuppressWarnings("removal")
        @Override
        void onInvocation(PlayerSpawnLocationEvent event) {
            onSpawnLocEvent(event.getPlayer().getUniqueId(), event.getSpawnLocation(), event::setSpawnLocation);
        }
    }
}