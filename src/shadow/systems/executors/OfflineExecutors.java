package shadow.systems.executors;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.multiengine.ban.BukkitBanList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldSaveEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import shadow.Main;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.login.Verifications;
import shadow.systems.login.autoin.PremiumAutoIn;
import shadow.systems.login.filters.ConnectionFilter;
import shadow.systems.login.result.ConnectionThreadManager;
import shadow.systems.login.result.LoginInfo;
import shadow.systems.login.result.LoginVerdictManager;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.main.file.managers.UserFileManager;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.users.UserManager;
import shadow.utils.users.offline.UnverifiedUser;
import shadow.utils.world.AlixWorld;
import shadow.utils.world.AlixWorldHolder;

import java.util.Arrays;

import static shadow.utils.main.AlixUtils.*;

public final class OfflineExecutors extends UniversalExecutors {

    //private final LoginAuthenticator authenticator = PremiumAutoIn.support;
    private final ConnectionFilter[] filters = AlixHandler.getConnectionFilters();
    private final String playerAlreadyOnlineMessage = Messages.get("player-already-online");
    private final AlixMessage
            joinCaptchaUnverified = Messages.getAsObject("log-player-join-captcha-unverified"),
            joinUnverified = Messages.getAsObject("log-player-join-unverified"),
            joinVerified = Messages.getAsObject("log-player-join-verified");
    //private static final BanList ipBanList = ConnectionAlgorithm.ipBanList;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        String name = e.getName();
        String address = e.getAddress().getHostAddress();
        //Main.logWarning("ADDRESS " + address);
        //The FireWall should handle unnecessary connections from being processed
        if (antibotService) ConnectionThreadManager.addJoinAttempt(name, address);
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        //Intentional concurrent check, as the ban Maps are overridden to their
        //concurrent equivalent in ReflectionUtils.replaceBansToConcurrent
        //Also, do not change the login result to let the server show the actual ban message
        //This logic might be changed in the future
        if (BukkitBanList.IP.isBanned(address) || BukkitBanList.NAME.isBanned(name))
            return;//prevent further processing, since #getLoginResult returns ALLOWED unless changed by another plugin

        if (name.startsWith("MC_STORM") || name.startsWith("BOT_")) {//primitive protection
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "AntiBot Protection");
            return;
        }

        if (isAlreadyOnline(name)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, playerAlreadyOnlineMessage);
            return;
        }

        PersistentUserData data = UserFileManager.get(name);

        if (data != null) {//the account exists
            if (PremiumAutoIn.remove(name)) LoginVerdictManager.addOnline(name, address, data, false);
            else LoginVerdictManager.addOffline(name, address, data);
            return;
        }

        if (PremiumAutoIn.remove(name)) {
            for (ConnectionFilter filter : premiumFilters) {
                if (filter.disallowJoin(address, name)) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                    return;
                }
            }
            LoginVerdictManager.addOnline(name, address, PersistentUserData.createFromPremiumInfo(name, address), true);
            return;
        }

        for (ConnectionFilter filter : filters) {
            if (filter.disallowJoin(address, name)) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                return;
            }
        }
        LoginVerdictManager.addOffline(name, address, data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnLocInit(PlayerSpawnLocationEvent event) {
        Location originalLoc = event.getSpawnLocation();
        //the join location is not in the captcha world
        if (originalLoc.getWorld().equals(AlixWorld.CAPTCHA_WORLD) || LoginVerdictManager.getExisting(event.getPlayer()).getVerdict().isAutoLogin())
            return;//the player doesn't need to be verified

        Player player = event.getPlayer();

        //Further handling helps in saving the location where the player currently is, even if dead
        Location loc = player.isDead() ? player.getBedSpawnLocation() : originalLoc;//if dead set to their spawn, original otherwise

        //If the bed location was null, get the world's default spawn
        OriginalLocationsManager.add(player, loc != null ? loc : originalLoc.getWorld().getSpawnLocation());//remember the original spawn location
        event.setSpawnLocation(AlixWorld.TELEPORT_LOCATION);//set the captcha world location as the spawn location (a faster onJoin teleport alternative)
    }

    //add the first in handling channel handler, after anyone else to prevent unnecessary packet processing
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        LoginInfo login = LoginVerdictManager.removeExisting(p);
        UnverifiedUser user = AlixHandler.handleOfflinePlayerJoin(p, e.getJoinMessage(), login);//the user can be null if the verification was not initialized - the user was premium or was auto-logged in by ip

        if (user != null && !user.hasCompletedCaptcha())
            e.setJoinMessage(null);//take priority in removing the join message for captcha unverified users

        if (!alixJoinLog) return;

        String name = p.getName();
        String ip = login.getIP(); //user != null ? user.getIPAddress() : e.getPlayer().getAddress().getAddress().getHostAddress();
        AlixMessage message = user != null ? !user.hasCompletedCaptcha() ? this.joinCaptchaUnverified : this.joinUnverified : this.joinVerified;

        Main.logInfo(message.format(name, ip));
    }
    /*        if (user != null) {
            if (!user.hasCompletedCaptcha()) {
                e.setJoinMessage(null);//take priority in removing the join message for captcha unverified users
                if (alixJoinLog)
                    Main.logInfo(joinCaptchaUnverified.format(e.getPlayer().getName(), user.getIPAddress()));
            } else if (alixJoinLog)
                Main.logInfo(joinUnverified.format(e.getPlayer().getName(), user.getIPAddress()));
        } else if (alixJoinLog)
            Main.logInfo(joinVerified.format(e.getPlayer().getName(), e.getPlayer().getAddress().getAddress().getHostAddress()));*/

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        AlixHandler.handleOfflinePlayerQuit(e.getPlayer(), e);
    }

    //Cancel before anyone else in order to signal
    //to other plugins to not process it themselves.
    //Uncancelling an event is just bad writing,
    //so it's not taken into account here
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!PacketBlocker.serverboundNameVersion) {//otherwise processed on the netty threads
            UnverifiedUser user = Verifications.get(player);
            char[] cmd = AlixCommandManager.getLowerCasedUnslashedCommand(e.getMessage());

            if (user != null) {
                e.setCancelled(true);
                AlixCommandManager.handleVerificationCommand(cmd, user);
                return;
            }

            String fullCommand = new String(cmd);
            String[] spletCommand = split(fullCommand, ' ');
            String commandLabel = spletCommand[0];

            if (AlixCommandManager.isPasswordChangeCommand(commandLabel)) {
                e.setCancelled(true);
                CommandManager.onPasswordChangeCommand(UserManager.getNullableUserOnline(player), Arrays.copyOfRange(spletCommand, 1, spletCommand.length));
                return;
            }
        }
        //ignore the cancellation up until this point
        if (!e.isCancelled() && isOperatorCommandRestricted)
            super.onOperatorCommandCheck(e, e.getMessage().substring(1));
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        if (!PacketBlocker.serverboundNameVersion && Verifications.has(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }
        super.onChat(e);
    }

    @EventHandler
    @Override
    public void onSave(WorldSaveEvent e) {
        if (userDataAutoSave && AlixWorldHolder.isMain(e.getWorld())) {
            AlixScheduler.async(() -> {
                UserFileManager.onAsyncSave();
                OriginalLocationsManager.onAsyncSave();
                FireWallManager.onAsyncSave();
            });
        }
    }
}