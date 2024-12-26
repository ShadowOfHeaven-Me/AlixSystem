package shadow.systems.executors;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.data.PersistentUserData;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.file.managers.IpsCacheFileManager;
import alix.common.utils.multiengine.ban.BukkitBanList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldSaveEvent;
import shadow.Main;
import shadow.systems.login.autoin.PremiumAccountCache;
import shadow.systems.login.result.LoginVerdictManager;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.main.file.managers.SpawnFileManager;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.users.Verifications;
import shadow.utils.world.AlixWorld;
import shadow.utils.world.AlixWorldHolder;

import static shadow.utils.main.AlixUtils.*;

public final class OfflineExecutors extends UniversalExecutors {

    //private final LoginAuthenticator authenticator = PremiumAutoIn.support;
    //private final ConnectionFilter[] filters = AlixHandler.getConnectionFilters();
    private final String playerAlreadyOnlineMessage = Messages.get("player-already-online");//,
    //serverIsFull = Messages.get("server-is-full");
    //private static final BanList ipBanList = ConnectionAlgorithm.ipBanList;


    //Pre-to-join executors - start

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        //Main.logInfo("ASYNC PRE LOGIN EVENT");
        String name = e.getName();
        String ip = e.getAddress().getHostAddress();
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

        //Main.logInfo("PREMIUM " + PremiumAutoIn.getPremiumPlayers().contains(name) + " DATA EXISTS " + (data != null));

        if (data != null) {//the account exists
            if (data.getPremiumData().getStatus().isPremium()) LoginVerdictManager.addOnline(name, ip, data, false, e);
            else LoginVerdictManager.addOffline(name, ip, data, e);
            return;
        }

        PremiumData premiumData = PremiumAccountCache.getOrUnknown(name);

        if (premiumData.getStatus().isPremium()) {
            /*for (ConnectionFilter filter : premiumFilters) {
                if (filter.disallowJoin(e.getAddress(), ip, name)) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                    return;
                }
            }*/
            LoginVerdictManager.addOnline(name, ip, PersistentUserData.createFromPremiumInfo(name, e.getAddress(), premiumData), true, e);
            PremiumAccountCache.remove(name);
            return;
        }

        /*for (ConnectionFilter filter : filters) {
            if (filter.disallowJoin(e.getAddress(), ip, name)) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                return;
            }
        }*/
        LoginVerdictManager.addOffline(name, ip, data, e);
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
        if (!verified) event.setRespawnLocation(AlixWorld.TELEPORT_LOCATION);
    }

    //Teleportation on login fix
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled() && event.getCause() == MethodProvider.ASYNC_TP_CAUSE && event.getFrom().getWorld().equals(AlixWorld.CAPTCHA_WORLD)) {//ensure the tp back from verification happens at all cost
            event.setCancelled(false);//look at me, saying how uncancelling an event is bad writing, and then doing it myself
            return;
        }
        if (!event.isCancelled() && !event.getTo().getWorld().equals(AlixWorld.CAPTCHA_WORLD) && event.getFrom().getWorld().equals(AlixWorld.CAPTCHA_WORLD) && event.getCause() != MethodProvider.ASYNC_TP_CAUSE && Verifications.has(event.getPlayer())) {
            OriginalLocationsManager.add(event.getPlayer(), event.getTo());
            event.setCancelled(true);
            //Bukkit.broadcastMessage("CANCELLED - " + UserManager.get(event.getPlayer().getUniqueId()).getClass().getSimpleName());
        }
    }

    //Damage during verification fix
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;

        if (Verifications.has(event.getEntity().getUniqueId())) event.setCancelled(true);
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