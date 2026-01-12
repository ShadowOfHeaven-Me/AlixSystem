package shadow.systems.virtualization.manager;

import alix.common.environment.ServerEnvironment;
import alix.common.utils.AlixCommonUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import shadow.Main;

final class UserSafeExecutors implements Listener {

    private final JoinEventManager.JoinEventExecutor joinEventExecutor;
    private final QuitEventManager.QuitEventExecutor quitEventExecutor;

    UserSafeExecutors() {
        this.joinEventExecutor = new JoinEventManager.JoinEventExecutor();
        this.quitEventExecutor = new QuitEventManager.QuitEventExecutor();
    }

    static void register() {
        boolean paper = ServerEnvironment.isPaper() && AlixCommonUtils.isValidClass("io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent");

        Listener spawnLocExec;

        if(paper) {
            spawnLocExec = new AsyncSpawnLocEvent();
            Main.logInfo("Initiating Paper's AsyncInitialLocation support");
        } else {
            spawnLocExec = new SyncSpawnLocEvent();
            Main.logInfo("Using Spigot SyncInitialLocation");
        }

        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new UserSafeExecutors(), Main.plugin);
        pm.registerEvents(spawnLocExec, Main.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onJoin(PlayerJoinEvent event) {
        this.joinEventExecutor.onInvocation(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onQuit(PlayerQuitEvent event) {
        this.quitEventExecutor.onInvocation(event);
    }
}