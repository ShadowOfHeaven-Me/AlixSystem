/*
package shadow.utils.objects.savable.data.password.pin.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import shadow.utils.objects.savable.data.password.pin.BuilderType;
import shadow.utils.objects.savable.data.password.pin.PasswordBuilder;

public class FancyPasswordBuilder implements PasswordBuilder {

    private final Inventory gui;
    private final Location location;

    public FancyPasswordBuilder(Location location) {
        this.location = location;
        this.gui = Bukkit.createInventory(null, 54, "Password: ");
    }

    @Override
    public Inventory getGUI() {
        return gui;
    }

    @Override
    public boolean select(int slot, Player player) {
        return false;
    }

    @Override
    public BuilderType getType() {
        return BuilderType.PIN;
    }

    @Override
    public String getPasswordBuilt() {
        return null;
    }
}*/
