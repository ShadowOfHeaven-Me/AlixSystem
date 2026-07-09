package shadow.systems.executors;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.data.PersistentUserData;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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

import java.util.concurrent.TimeUnit;

import static shadow.utils.main.AlixUtils.*;

public final class OfflineExecutors extends UniversalExecutors {

    private final String playerAlreadyOnlineMessage = Messages.get("player-already-online");//,
    private static final boolean canOverride = assignPremiumUUID && ServerEnvironment.isPaper() && PaperReflection.isAvailable();

    //Pre-to-join executors - start

    private void overrideUUIDIfNecessary(AsyncPlayerPreLoginEvent e, PersistentUserData data, String name, User user) {
        if (!assignPremiumUUID)
            return;

        var uuid = e.getUniqueId();
        //boolean isPremium = VerifiedCache.isPremium(data, name, user);

        if (uuid.version() == 3 && VerifiedCache.isPremium(data, name, user)) {
            if (canOverride) {
                var premiumUUID = data.getPremiumData().premiumUUID();
                PaperReflection.override(e, premiumUUID);

                Main.logInfo("Late overriding of player's " + name + " non-premium uuid " + uuid + " with premium uuid " + premiumUUID);
            } else
                Main.logWarning("Player " + name + " could not have his uuid late set to premium, despite config 'premium-uuid: true' & premium status! Report this as an error immediately!");
            //return;
        }

        //hmmm
        //disabled, for now
        /*if (uuid.version() == 4 && !isPremium) {
            if (canOverride) {
                var nonPremiumUUID = UUIDUtil.getOfflineModeUuid(name);
                PaperReflection.override(e, nonPremiumUUID);

                Main.logInfo("Late overriding of player's " + name + " premium uuid " + uuid + " with non-premium uuid " + nonPremiumUUID);
            } else
                Main.logWarning("Player " + name + " could not have his uuid late set to non-premium, despite config 'premium-uuid: true' & a non-premium status! Report this as an error immediately!");
        }*/
    }

    //per https://github.com/LuckPerms/LuckPerms/blob/65c42b9b09be6510992c29b2f29b67bffb740232/bukkit/src/main/java/me/lucko/luckperms/bukkit/listeners/BukkitConnectionListener.java#L88
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        String name = e.getName();
        String ip = e.getAddress().getHostAddress();

        User user = UserManager.removeConnecting(name);//name

        if (user == null) {
            e.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No user, AlixSystem)");
            return;
        }

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

        if (data != null) {//the account exists
            if (isLinked) {
                LoginVerdictManager.addOnline(user, ip, data, false, e);
                return;
            }
            //Main.logInfo("isPremium()=" + data.getPremiumData().getStatus().isPremium() + " __noPremiumAuthButKeepIdentity=" + __noPremiumAuthButKeepIdentity + " VerifiedCache=" + VerifiedCache.getAndCheckIfEquals(name, user));

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

        if (ONLINE_MODE || VerifiedCache.removeAndCheckIfEquals(name, user)) {
            PremiumData premiumData = PremiumUtils.getOrRequestAndCacheDataSync(null, name);
            if (!ONLINE_MODE && !premiumData.getStatus().isPremium()) {
                Main.logWarning("PremiumData " + premiumData.getStatus() + " clarified per PremiumDataCache is not premium, but was in the VerifiedCache! Report this immediately!");
            }

            LoginVerdictManager.addOnline(user, ip, PersistentUserData.createFromPremiumInfo(name, e.getAddress(), premiumData), true, e);
            return;
        }
        LoginVerdictManager.addOffline(user, ip, data, e);
    }
    //Pre-to-join executors - end

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
        if (event.isCancelled() && event.getCause() == MethodProvider.ASYNC_TP_CAUSE && from.getWorld().equals(AlixWorld.CAPTCHA_WORLD) && !to.getWorld().equals(AlixWorld.CAPTCHA_WORLD)) {//ensure the tp back from verification happens at all cost
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

    //Hunger during verification fix
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player player)) return;

        if (Verifications.has(player))
            event.setCancelled(true);
    }

    //Damage during verification fix
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player player)) return;

        if (Verifications.has(player))
            event.setCancelled(true);
    }
    //During verification executors - end

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) return;

        if (isOperatorCommandRestricted)
            super.onOperatorCommandCheck(e, e.getMessage().substring(1));

        if (Verifications.has(e.getPlayer())) {
            e.setCancelled(true);
            Main.logWarning("Player " + e.getPlayer().getName() + " tried executing command while unverified - cancelling! Report this if this is a security error!");
        }
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        super.onChat(e);
    }

    static {
        AlixScheduler.repeatAsync(OfflineExecutors::saveFiles, 1, TimeUnit.MINUTES);
    }

    static void saveFiles() {
        UserFileManager.onAsyncSave();
        OriginalLocationsManager.onAsyncSave();
        FireWallManager.onAsyncSave();
        IpsCacheFileManager.save();
        UserTokensFileManager.save();
        AllowListFileManager.save();
    }

    @EventHandler
    public void onSave(WorldSaveEvent e) {
        if (userDataAutoSave && AlixWorldHolder.isMain(e.getWorld())) {
            AlixScheduler.async(OfflineExecutors::saveFiles);
        }
    }
}