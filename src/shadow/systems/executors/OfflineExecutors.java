package shadow.systems.executors;

import alix.common.antibot.connection.ConnectionFilter;
import alix.common.messages.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldSaveEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.aliases.FileCommandManager;
import shadow.systems.login.Verifications;
import shadow.systems.login.autoin.PremiumAutoIn;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.main.file.managers.UserFileManager;
import shadow.utils.objects.filter.packet.types.PacketBlocker;
import shadow.utils.users.offline.UnverifiedUser;
import shadow.utils.world.AlixWorld;

import static shadow.utils.main.AlixUtils.*;

public final class OfflineExecutors extends UniversalExecutor {

    //private final LoginAuthenticator authenticator = PremiumAutoIn.support;
    private final ConnectionFilter[] filters = AlixHandler.getConnectionFilters();
    private final String playerAlreadyOnlineMessage = Messages.get("player-already-online");

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        String name = e.getName();

        if (name.startsWith("MC_STORM") || name.startsWith("BOT_")) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "AntiBot Protection");
            return;
        }

        if (isAlreadyOnline(name)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, playerAlreadyOnlineMessage);
            return;
        }

        if (UserFileManager.hasName(name)) return;

        if (PremiumAutoIn.contains(name)) {

            String address = e.getAddress().getHostAddress();

            for (ConnectionFilter filter : premiumFilters) {
                if (filter.disallowJoin(address, name)) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                    break;
                }
            }
            return;
        }

        String address = e.getAddress().getHostAddress();

        for (ConnectionFilter filter : filters) {
            if (filter.disallowJoin(address, name)) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnLocInit(PlayerSpawnLocationEvent event) {
        if (requireCaptchaVerification && !event.getSpawnLocation().getWorld().equals(AlixWorld.CAPTCHA_WORLD) && !UserFileManager.hasName(event.getPlayer().getName())) {//the player has not completed the captcha verification
            //the join location is not in the captcha world
            OriginalLocationsManager.add(event.getPlayer(), event.getSpawnLocation());//remember the original spawn location
            event.setSpawnLocation(AlixWorld.TELEPORT_LOCATION);//set the captcha world as the spawn location
        }
        //event.setSpawnLocation(AlixHandler.handleOfflinePlayerJoin(event.getPlayer(), event.getSpawnLocation()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    //add the channel handler after anyone else to remove unnecessary packet processing
    public void onJoin(PlayerJoinEvent e) {
        UnverifiedUser user = AlixHandler.handleOfflinePlayerJoin(e.getPlayer(), e.getJoinMessage());//user can be null if the verification was not initialized - the user was premium or was auto-logged in by ip

        if (user != null && !user.hasCompletedCaptcha())
            e.setJoinMessage(null);//take priority in removing the join message for captcha unverified users
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        AlixHandler.handleOfflinePlayerQuit(e.getPlayer(), e);
    }

/*    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        event.setCompletions(UserManager.notVanishedUserNicknames);
    }*/

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        String cmd = e.getMessage().substring(1).toLowerCase();
        UnverifiedUser user = Verifications.get(p);

        if (user != null) {
            if (user.isGUIInitialized()) {
                e.setCancelled(true);
                return;
            }
            String[] split = split(cmd, ' ');
            String split0 = split[0];

            if (FileCommandManager.isLoginCommand(split0)) {
                String verificationCommand = FileCommandManager.getCommand(split0).getCommand();
                if (split.length == 1) {
                    switch (verificationCommand) {
                        case "login":
                            sendMessage(p, CommandManager.formatLogin);
                            break;
                        case "register":
                            sendMessage(p, CommandManager.formatRegister);
                            break;
                        case "captcha":
                            sendMessage(p, CommandManager.formatCaptcha);
                            break;
                    }
                    return;
                }

                String arg2 = split[1];

                if (verificationCommand.equals("captcha")) {
                    CommandManager.onCaptchaCommand(user, p, arg2);
                    e.setCancelled(true);
                    return;
                }

/*                String hashedPassword;

                if (data == null) hashedPassword = Hashing.getConfigHashingAlgorithm().hash(arg2);
                else hashedPassword = data.getPassword().getHashingAlgorithm().hash(arg2);*/

                if (verificationCommand.equals("login")) CommandManager.onLoginCommand(user, p, arg2);
                else CommandManager.onRegisterCommand(user, p, arg2);
                //e.setMessage("/" + split0 + " " + hashedPassword);
            }
            e.setCancelled(true);
            return;
        }

        if (!isOperatorCommandRestricted) return;
        super.onCommand(e, cmd);
    }

/*    @EventHandler
    public void onReloadCommand(ServerCommandEvent e) {
        String cmd = removeSlash(e.getCommand().toLowerCase());
        if (e.isCancelled()) return;
        handleReloadCommand(cmd);
    }*/

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        if (!PacketBlocker.serverboundVersion && Verifications.has(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }
        super.onChat(e);
    }

    @EventHandler
    @Override
    public void onSave(WorldSaveEvent e) {
        if (userDataAutoSave && mainWorldUUID.equals(e.getWorld().getUID())) {
            UserFileManager.asyncSave();
            OriginalLocationsManager.asyncSave();
        }
    }

    /*    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (getUser(e.getPlayer()).isNotLoggedIn()) e.setCancelled(true);
    }*/

/*    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        Entity dam = e.getDamager();
        Entity vic = e.getEntity();
        if (dam instanceof Player && getUserOnline((Player) dam).isLoggedIn() || vic instanceof Player && getUserOnline((Player) vic).isLoggedIn())
            e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!getUserOnline(e.getPlayer()).isLoggedIn()) e.setCancelled(true);
    }*/

/*    @EventHandler
    public void onInv(InventoryInteractEvent e) {
        HumanEntity en = e.getWhoClicked();
        if (en instanceof Player && !getUserOnline((Player) en).isLoggedIn()) e.setCancelled(true);
    }

    @EventHandler
    public void onInv(InventoryClickEvent e) {
        HumanEntity en = e.getWhoClicked();
        if (en instanceof Player && !getUserOnline((Player) en).isLoggedIn()) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!getUserOnline(e.getPlayer()).isLoggedIn()) e.setCancelled(true);
    }*/
}