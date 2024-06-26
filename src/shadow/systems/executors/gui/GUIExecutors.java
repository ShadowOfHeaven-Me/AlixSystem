package shadow.systems.executors.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.systems.gui.AlixGUI;

public final class GUIExecutors implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)//can be
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        AbstractAlixGUI gui = AlixGUI.MAP.get(event.getWhoClicked().getUniqueId());
        if (gui != null) {
            event.setCancelled(true);//have this be set first, in case an error occurs in the latter method
            gui.onClick(event);
        }
    }

    //do not remove the gui if the closed inventory exists and is different from the closed one, since a gui could've just opened another gui
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInvClose(InventoryCloseEvent event) {
        //Main.logError("GUI OPEN: " + AlixGUI.MAP.get(event.getPlayer().getUniqueId()));
        AlixGUI.MAP.compute(event.getPlayer().getUniqueId(), (u, g) -> g != null && !g.getGUI().equals(event.getInventory()) ? g : null);
        //Main.logError("GUI OPEN 2: " + AlixGUI.MAP.get(event.getPlayer().getUniqueId()));
    }

//if (!(event.getPlayer() instanceof Player)) return;
//AlixGUI.MAP.remove(event.getPlayer().getUniqueId());

    /*UnverifiedUser user = Verifications.get(event.getPlayer().getUniqueId());

    if (user == null) return;//user.isPinGUIInitialized();

    if (user.isGuiUser() && user.isGUIInitialized() && user.getPasswordBuilder().getType() == BuilderType.PIN) {
            AlixScheduler.runLaterSync(user::openPasswordBuilderGUI, 100, TimeUnit.MILLISECONDS);
    }*/
}