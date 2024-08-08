package shadow.systems.executors;

import alix.common.scheduler.AlixScheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldSaveEvent;
import alix.common.connection.filters.ConnectionFilter;
import alix.common.data.file.UserFileManager;
import shadow.utils.world.AlixWorldHolder;

import static shadow.utils.main.AlixUtils.isOperatorCommandRestricted;
import static shadow.utils.main.AlixUtils.userDataAutoSave;
import static shadow.utils.users.UserManager.addOnlineUser;
import static shadow.utils.users.UserManager.remove;

public final class OnlineExecutors extends UniversalExecutors {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        String name = e.getName();
        String address = e.getAddress().getHostAddress();

        for (ConnectionFilter filter : premiumFilters) {
            if (filter.disallowJoin(e.getAddress(), address, name)) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, filter.getReason());
                break;
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        addOnlineUser(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

/*    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        event.setCompletions(UserManager.notVanishedUserNicknames);
    }*/

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!isOperatorCommandRestricted || e.isCancelled()) return;
        String cmd = e.getMessage().substring(1).toLowerCase();
        super.onOperatorCommandCheck(e, cmd);
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        super.onChat(e);
    }

    @EventHandler
    public void onSave(WorldSaveEvent e) {
        if (userDataAutoSave && AlixWorldHolder.isMain(e.getWorld())) AlixScheduler.async(UserFileManager::onAsyncSave);
        //WarpFileManager.save(); For now disabled
    }

/*    @EventHandler
    public void onReloadCommand(ServerCommandEvent e) {
        if (e.isCancelled()) return;
        handleReloadCommand(removeSlash(e.getCommand().toLowerCase()));
    }*/
}