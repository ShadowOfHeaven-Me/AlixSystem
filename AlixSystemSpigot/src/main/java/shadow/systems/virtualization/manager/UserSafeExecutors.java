package shadow.systems.virtualization.manager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import shadow.Main;

final class UserSafeExecutors implements Listener {

    private final JoinEventManager.JoinEventExecutor joinEventExecutor;
    private final QuitEventManager.QuitEventExecutor quitEventExecutor;
    private final SpawnLocEventManager.SpawnEventExecutor spawnEventExecutor;

    UserSafeExecutors() {
        this.joinEventExecutor = new JoinEventManager.JoinEventExecutor();
        this.quitEventExecutor = new QuitEventManager.QuitEventExecutor();
        this.spawnEventExecutor = new SpawnLocEventManager.SpawnEventExecutor();
    }

    static void register() {
        Bukkit.getPluginManager().registerEvents(new UserSafeExecutors(), Main.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onSpawn(PlayerSpawnLocationEvent event) {
        this.spawnEventExecutor.onInvocation(event);
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