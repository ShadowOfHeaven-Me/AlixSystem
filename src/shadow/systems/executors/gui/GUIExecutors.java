package shadow.systems.executors.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import shadow.systems.login.Verifications;
import shadow.utils.objects.savable.data.gui.AlixGui;
import shadow.utils.users.offline.UnverifiedUser;

public final class GUIExecutors implements Listener {

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        UnverifiedUser user = Verifications.get(event.getWhoClicked().getUniqueId());
        if (user == null) return;

        event.setCancelled(true);

        if (!user.isGuiUser() || !user.isGUIInitialized()) //not a pin user or the gui has not been initialized yet
            return;

        AlixGui.onSyncClick(user, event.getRawSlot());
    }

/*    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        UnverifiedUser user = Verifications.get(event.getPlayer().getUniqueId());

        if (user == null) return;//user.isPinGUIInitialized();

        if (user.isGuiUser() && user.isGUIInitialized() && user.getPasswordBuilder().getType() == BuilderType.PIN) {
            AlixScheduler.runLaterSync(user::openPasswordBuilderGUI, 100, TimeUnit.MILLISECONDS);
        }
    }*/
}