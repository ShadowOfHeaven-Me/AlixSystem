package shadow.systems.executors;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.data.PersistentUserData;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.data.premium.VerifiedCache;
import alix.common.data.security.password.Password;
import alix.common.environment.ServerEnvironment;
import alix.common.login.premium.PremiumUtils;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.file.managers.IpsCacheFileManager;
import alix.common.utils.multiengine.ban.BukkitBanList;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldSaveEvent;
import shadow.Main;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.login.paper.PaperReflection;
import shadow.systems.login.result.LoginVerdictManager;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.main.file.managers.SpawnFileManager;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.users.UserManager;
import shadow.utils.users.Verifications;
import shadow.utils.world.AlixWorld;
import shadow.utils.world.AlixWorldHolder;
import ua.nanit.limbo.util.UUIDUtil;

import static shadow.utils.main.AlixUtils.*;

public final class OfflineExecutors extends UniversalExecutors {

    //private final LoginAuthenticator authenticator = PremiumAutoIn.support;
    //private final ConnectionFilter[] filters = AlixHandler.getConnectionFilters();
    private final String playerAlreadyOnlineMessage = Messages.get("player-already-online");//,
    //serverIsFull = Messages.get("server-is-full");
    //private static final BanList ipBanList = ConnectionAlgorithm.ipBanList;
    private final boolean onlineMode = Bukkit.getServer().getOnlineMode();
    private static final boolean canOverride = assignPremiumUUID && ServerEnvironment.isPaper() && PaperReflection.isAvailable();

    //Pre-to-join executors - start

    private void overrideUUIDIfNecessary(AsyncPlayerPreLoginEvent e, PersistentUserData data, String name, User user) {
        if (!assignPremiumUUID)
            return;

        var uuid = e.getUniqueId();
        boolean isPremium = VerifiedCache.isPremium(data, name, user);

        if (uuid.version() == 3 && isPremium) {
            if (canOverride) {
                var premiumUUID = data.getPremiumData().premiumUUID();
                PaperReflection.override(e, premiumUUID);

                Main.logInfo("Late overriding of player's " + name + " non-premium uuid " + uuid + " with premium uuid " + premiumUUID);
            } else
                Main.logWarning("Player " + name + " could not have his uuid late set to premium, despite config 'premium-uuid: true' & premium status! Report this as an error immediately!");
            return;
        }

        //hmmm
        if (uuid.version() == 4 && !isPremium) {
            if (canOverride) {
                var nonPremiumUUID = UUIDUtil.getOfflineModeUuid(name);
                PaperReflection.override(e, nonPremiumUUID);

                Main.logInfo("Late overriding of player's " + name + " premium uuid " + uuid + " with non-premium uuid " + nonPremiumUUID);
            } else
                Main.logWarning("Player " + name + " could not have his uuid late set to non-premium, despite config 'premium-uuid: true' & a non-premium status! Report this as an error immediately!");

        }
    }

    //per https://github.com/LuckPerms/LuckPerms/blob/65c42b9b09be6510992c29b2f29b67bffb740232/bukkit/src/main/java/me/lucko/luckperms/bukkit/listeners/BukkitConnectionListener.java#L88
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        //Main.logInfo("ASYNC PRE LOGIN EVENT");
        String name = e.getName();
        String ip = e.getAddress().getHostAddress();

        //Main.debug("onLogin: '" + name + "'");
        User user = UserManager.removeConnecting(name);//name

        //Main.logError("NAME IN EVENT: " + name);

        if (user == null) {
            e.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No user, AlixSystem)");
            return;
        }

        //Main.logError("WWWWWWWWWWWW " + PremiumAutoIn.isPremium(e.getUniqueId()));
        //The FireWall should handle unnecessary connections from being processed

        //This logic was moved to AlixChannelHandler
        //if (antibotService) ConnectionThreadManager.onJoinAttempt(name, e.getAddress());

        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        /*if (Bukkit.getMaxPlayers() <= UserManager.userCount()) {//we have to perform this check due to semi-virtualization problems - the server does not really know how many players there currently are on the server
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, this.serverIsFull);
            return;
        }*/
        //Intentional concurrent check, as the ban Maps are overridden to their
        //concurrent equivalent in ReflectionUtils.replaceBansToConcurrent
        //Also, do not change the login result to let the server show the actual ban message
        //This logic might be changed in the future
        if (BukkitBanList.IP.isBanned(ip) || BukkitBanList.NAME.isBanned(name))
            return;//prevent further processing, since #getLoginResult returns ALLOWED, unless changed by another plugin

        if (name.startsWith("MC_STORM") || name.startsWith("BOT_")) {//primitive protection
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "AntiBot Protection");
            return;
        }

        if (isAlreadyOnline(name)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.playerAlreadyOnlineMessage);
            return;
        }

        PersistentUserData data = UserFileManager.get(name);
        boolean isLinked = Dependencies.isLinked((Channel) user.getChannel());

        this.overrideUUIDIfNecessary(e, data, name, user);
        //Main.logInfo("PREMIUM " + PremiumAutoIn.getPremiumPlayers().contains(name) + " DATA EXISTS " + (data != null));

        if (data != null) {//the account exists
            if (isLinked) {
                LoginVerdictManager.addOnline(user, ip, data, false, e);
                return;
            }

            if (data.getPremiumData().getStatus().isPremium() && !__noPremiumAuthButKeepIdentity && VerifiedCache.removeAndCheckIfEquals(name, user))
                LoginVerdictManager.addOnline(user, ip, data, false, e);
            else
                LoginVerdictManager.addOffline(user, ip, data, e);
            return;
        }

        if (isLinked) {
            LoginVerdictManager.addOnline(user, ip, PersistentUserData.createDefault(name, e.getAddress(), Password.createRandom()), true, e);
            return;
        }

        if (this.onlineMode || VerifiedCache.removeAndCheckIfEquals(name, user)) {
            /*for (ConnectionFilter filter : premiumFilters) {
                if (filter.disallowJoin(e.getAddress(), ip, name)) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                    return;
                }
            }*/
            PremiumData premiumData = PremiumDataCache.removeOrUnknown(name);
            if (!this.onlineMode && !premiumData.getStatus().isPremium()) {
                Main.logWarning("PremiumData " + premiumData.getStatus() + " clarified per PremiumDataCache is not premium, but was in the VerifiedCache! Report this immediately!");

                if (premiumData.getStatus().isUnknown()) premiumData = PremiumUtils.requestPremiumData(name);
            }

            LoginVerdictManager.addOnline(user, ip, PersistentUserData.createFromPremiumInfo(name, e.getAddress(), premiumData), true, e);
            return;
        }

        /*for (ConnectionFilter filter : filters) {
            if (filter.disallowJoin(e.getAddress(), ip, name)) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                return;
            }
        }*/
        LoginVerdictManager.addOffline(user, ip, data, e);
    }
    //Pre-to-join executors - end

    //Moved to UserSemiVirtualization and related

    /*@EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnLocInit(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        Location joinLoc = event.getSpawnLocation();

        boolean verified = LoginVerdictManager.get(event.getPlayer()).isVerified();

        //the join location is in the captcha world
        if (joinLoc.getWorld().getUID().equals(AlixWorld.CAPTCHA_WORLD.getUID())) {
            event.setSpawnLocation(verified ? OriginalLocationsManager.getOriginalLocation(player) : AlixWorld.TELEPORT_LOCATION);//ensure it's at the correct location
            return;
        }

        if (verified) return;//the player doesn't need to be teleported for verification

        //The join location was not in the captcha world

        //Further handling helps in saving the actual original location, even if dead
        Location originLoc = player.isDead() ? player.getBedSpawnLocation() : joinLoc;//if dead set to their spawn, original otherwise

        //Check whether the original location is valid
        if (originLoc != null && !originLoc.getWorld().getUID().equals(AlixWorld.CAPTCHA_WORLD.getUID()))
            OriginalLocationsManager.add(player, originLoc);//remember the original spawn location

        event.setSpawnLocation(AlixWorld.TELEPORT_LOCATION);//set the captcha world location as the spawn location (a faster onJoin teleport alternative)
    }*/

    //The contract is that both of these are constant - the exact same, but are not the very same instance - they just contain the exact same contents
    //Normally this is done by comparing the values when converted to bits, so that no inaccuracies created by computer math occur
/*    @AlixIntrinsified(method = "Location#equals")
    public static boolean fastConstEqual(Location loc1, Location loc2) {
        return loc1.getWorld().getUID().equals(loc2.getWorld().getUID()) && loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY()
                && loc1.getZ() == loc2.getZ() && loc1.getPitch() == loc2.getPitch() && loc1.getYaw() == loc2.getYaw();
    }*/

/*    private static boolean exactRotation(Location loc1, Location loc2) {
        return loc1.getPitch() == loc2.getPitch() && loc1.getYaw() == loc2.getYaw();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Main.logInfo("ON ORIGINAL JOIN");
        *//*Player p = e.getPlayer();
        TemporaryUser tempUser = LoginVerdictManager.get(p);
        UnverifiedUser user = AlixHandler.handleVirtualPlayerJoin(p, , tempUser);//the user can be null if the verification was not initialized - the user was premium or was auto-logged in by ip

        if (user != null && !user.hasCompletedCaptcha()) {
            e.setJoinMessage(null);//take priority in removing the join message for captcha unverified users
            //teleport once to the ideal rotation, since PlayerSpawnLocationEvent doesn't do it for some reason
            if (!exactRotation(p.getLocation(), AlixWorld.TELEPORT_LOCATION))
                MethodProvider.teleportAsync(p, AlixWorld.TELEPORT_LOCATION);
        }

        if (!alixJoinLog) return;

        String name = p.getName();
        String ip = tempUser.getLoginInfo().getIP(); //user != null ? user.getIPAddress() : e.getPlayer().getAddress().getAddress().getHostAddress();
        AlixMessage message = user != null ? !user.hasCompletedCaptcha() ? this.joinCaptchaUnverified : this.joinUnverified : this.joinVerified;

        Main.logInfo(message.format(name, ip));*//*
    }*/
    /*        if (user != null) {
            if (!user.hasCompletedCaptcha()) {
                e.setJoinMessage(null);//take priority in removing the join message for captcha unverified users
                if (alixJoinLog)
                    Main.logInfo(joinCaptchaUnverified.format(e.getPlayer().getName(), user.getIPAddress()));
            } else if (alixJoinLog)
                Main.logInfo(joinUnverified.format(e.getPlayer().getName(), user.getIPAddress()));
        } else if (alixJoinLog)
            Main.logInfo(joinVerified.format(e.getPlayer().getName(), e.getPlayer().getAddress().getAddress().getHostAddress()));*/

    //During verification executors - start

    //spawn in verification world fix

    private final boolean useLastLoc = Main.config.getBoolean("use-last-location-as-fallback");

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(PlayerRespawnEvent event) {
        boolean verified = !Verifications.has(event.getPlayer());
        if (verified && event.getRespawnLocation().getWorld().equals(AlixWorld.CAPTCHA_WORLD)) {
            event.setRespawnLocation(this.useLastLoc ? OriginalLocationsManager.getOriginalLocation(event.getPlayer()) : SpawnFileManager.getSpawnLocation());
            return;
        }
        if (!verified)
            event.setRespawnLocation(AlixWorld.TELEPORT_LOCATION);
    }

    //Teleportation on login fix
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (event.isCancelled() && event.getCause() == MethodProvider.ASYNC_TP_CAUSE && from.getWorld().equals(AlixWorld.CAPTCHA_WORLD)) {//ensure the tp back from verification happens at all cost
            event.setCancelled(false);//look at me, saying how uncancelling an event is bad writing, and then doing it myself
            return;
        }
        Player player = event.getPlayer();
        if (!event.isCancelled() && !to.getWorld().equals(AlixWorld.CAPTCHA_WORLD) && from.getWorld().equals(AlixWorld.CAPTCHA_WORLD) && event.getCause() != MethodProvider.ASYNC_TP_CAUSE && Verifications.has(player)) {
            OriginalLocationsManager.add(player, to);
            event.setCancelled(true);
            //Bukkit.broadcastMessage("CANCELLED - " + UserManager.get(event.getPlayer().getUniqueId()).getClass().getSimpleName());
        }
    }

    //Damage during verification fix
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player player)) return;

        if (Verifications.has(player.getUniqueId()))
            event.setCancelled(true);
    }
    //During verification executors - end

/*    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        Main.logInfo("ON KNOWN QUIT");
        //AlixHandler.handleVirtualPlayerQuit(e.getPlayer(), e);
    }*/

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!e.isCancelled() && isOperatorCommandRestricted)
            super.onOperatorCommandCheck(e, e.getMessage().substring(1));
        if (!e.isCancelled() && Verifications.has(e.getPlayer())) {
            e.setCancelled(true);
            Main.logWarning("Player " + e.getPlayer().getName() + " tried executing command while unverified - cancelling! Report this if this is a security error!");
        }
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        /*if (!PacketBlocker.serverboundNameVersion && Verifications.has(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }*/
        super.onChat(e);
    }

    @EventHandler
    public void onSave(WorldSaveEvent e) {
        if (userDataAutoSave && AlixWorldHolder.isMain(e.getWorld())) {
            AlixScheduler.async(() -> {
                UserFileManager.onAsyncSave();
                OriginalLocationsManager.onAsyncSave();
                FireWallManager.onAsyncSave();
                IpsCacheFileManager.save();
                UserTokensFileManager.save();
                AllowListFileManager.save();
            });
        }
    }
}