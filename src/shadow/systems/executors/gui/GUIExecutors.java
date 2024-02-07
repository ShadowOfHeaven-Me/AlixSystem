package shadow.systems.executors.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.systems.gui.AlixGUI;
import shadow.systems.login.Verifications;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.UUID;

public final class GUIExecutors implements Listener {

    @EventHandler
    public final void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        UUID uuid = event.getWhoClicked().getUniqueId();
        UnverifiedUser user = Verifications.get(uuid);

        if (user == null) {
            AbstractAlixGUI gui = AlixGUI.MAP.get(uuid);
            if (gui != null) {
                gui.onClick(event);
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);

        if (!user.isGuiUser() || !user.isGUIInitialized()) //not a pin user or the gui has not been initialized yet
            return;

        AlixVerificationGui.onSyncClick(user, event);
    }

    //do not remove the gui if the closed inventory exists and is different from the closed one, since a gui could've just opened another gui
    @EventHandler
    public final void onInvClose(InventoryCloseEvent event) {
        AlixGUI.MAP.compute(event.getPlayer().getUniqueId(), (u, g) -> g != null && !g.getGUI().equals(event.getInventory()) ? g : null);
    }

    //if (!(event.getPlayer() instanceof Player)) return;
    //AlixGUI.MAP.remove(event.getPlayer().getUniqueId());

    /*UnverifiedUser user = Verifications.get(event.getPlayer().getUniqueId());

    if (user == null) return;//user.isPinGUIInitialized();

    if (user.isGuiUser() && user.isGUIInitialized() && user.getPasswordBuilder().getType() == BuilderType.PIN) {
            AlixScheduler.runLaterSync(user::openPasswordBuilderGUI, 100, TimeUnit.MILLISECONDS);
    }*/
}