package shadow.systems.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface AbstractAlixGUI {

    void onClick(InventoryClickEvent event);

    Inventory getGUI();

}